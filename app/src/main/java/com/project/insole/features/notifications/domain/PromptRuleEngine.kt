package com.project.insole.features.notifications.domain

import com.project.insole.features.sensor.domain.model.Clock
import com.project.insole.features.sensor.domain.model.ClinicalScoringVariables
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.model.SystemClock
import kotlin.math.abs

/** How a prompt is presented. Patient-facing severities are deliberately calm:
 *  there is no "critical" here — that language belongs to the doctor app. */
enum class PromptSeverity { ATTENTION, INFO }

/** Overall patient-facing status for the hero card. */
enum class OverallStatus { ALL_CLEAR, ATTENTION, NO_DATA }

enum class FootSide { LEFT, RIGHT }

/**
 * One prompt decision from the rule engine — semantics only, no display
 * copy. The ViewModel maps these to the calm wording (plan MD S2/S3) and
 * forwards [thresholdVersion] into the future sync batches.
 */
data class DomainPrompt(
    val id: String,                     // THERMAL_LEFT, PRESSURE_RIGHT, NO_DATA
    val severity: PromptSeverity,
    val foot: FootSide?,
    /** Rule regime that produced this prompt (ThresholdsConfig.version). */
    val thresholdVersion: String,
    val riskLevel: com.project.insole.features.sensor.domain.model.RiskLevel = com.project.insole.features.sensor.domain.model.RiskLevel.LOW,
    val isValidSwell: Boolean = false
)

data class RuleInput(
    val leftConnected: Boolean,
    val rightConnected: Boolean,
    val sensorData: InsoleSensorData?,
    val clinicalVariables: ClinicalScoringVariables? = null
)

data class PromptEvaluation(
    val overallStatus: OverallStatus,
    val prompts: List<DomainPrompt>
)

/**
 * Domain rule evaluator for patient prompts (TD-8: moved out of the
 * ViewModel so it is pure, injectable-time, and unit-testable).
 *
 * Rules (docs/design/UX-BRAND-SPEC.md + patient plan MD):
 *  - A single abnormal reading never creates a prompt — the thermal rule
 *    requires [ThresholdsConfig.thermalPersistenceCount] consecutive
 *    evaluations; persistence is truly consecutive (a quiet evaluation
 *    resets the count).
 *  - Prompts are rate-limited per key (cooldown) so support can never
 *    become nagging.
 *  - "No recent data" is never presented as "All clear".
 *  - Every prompt carries the [ThresholdsConfig.version] that produced it.
 *
 * The engine is stateful (persistence counters + cooldown stamps) and must
 * be fed through [evaluate] from a single caller — the ViewModel's
 * collectors serialise on the main dispatcher. Time comes from the injected
 * [Clock] (monotonic), so tests use [FakeClock] and production never sees a
 * wall-clock jump.
 */
class PromptRuleEngine(
    private val clock: Clock = SystemClock,
    initialConfig: ThresholdsConfig = ThresholdsConfig.DEFAULT
) {

    var config: ThresholdsConfig = initialConfig
        private set

    private val lastShownAtMs = mutableMapOf<String, Long>()
    private val consecutiveCounts = mutableMapOf<String, Int>()

    /**
     * Installs a new thresholds config (e.g. fetched from the cloud). A
     * version change resets persistence counters and cooldowns — the new
     * regime must start clean.
     */
    fun updateConfig(new: ThresholdsConfig) {
        if (new == config) return
        config = new
        lastShownAtMs.clear()
        consecutiveCounts.clear()
    }

    fun evaluate(input: RuleInput): PromptEvaluation {
        val anyConnected = input.leftConnected || input.rightConnected
        // ── True disconnected state: neither insole is connected or telemetry is absent ──
        if (!anyConnected || input.sensorData == null) {
            return PromptEvaluation(
                overallStatus = OverallStatus.NO_DATA,
                prompts = listOf(
                    DomainPrompt("NO_DATA", PromptSeverity.INFO, null, config.version)
                )
            )
        }

        val prompts = mutableListOf<DomainPrompt>()
        val data = input.sensorData
        val clinical = input.clinicalVariables
        val effectiveWarning = config.effectivePressureWarning()
        val isStanceOrUnspecified = clinical == null || clinical.isCurrentlyActiveStance || clinical.isCurrentlyWornClean
        val hasBothFeetForThermal = input.leftConnected && input.rightConnected

        // ── Clinical Risk Scoring Function ──────────────────────────────────
        // Thermal exceed strictly requires bilateral data (both feet present)
        val thermalExceed = if (hasBothFeetForThermal) {
            if (clinical != null) {
                clinical.thermalExceed
            } else {
                if (data.leftTemperature > 0f && data.rightTemperature > 0f && abs(data.leftTemperature - data.rightTemperature) >= config.thermalDeltaWarningC) 1 else 0
            }
        } else {
            0
        }

        // Pressure exceed evaluates for whichever foot is connected and active
        val pressureExceed = if (clinical != null && clinical.pressureExceed == 1) {
            1
        } else if (isStanceOrUnspecified && (
            (input.leftConnected && data.leftPressure >= effectiveWarning) ||
            (input.rightConnected && data.rightPressure >= effectiveWarning)
        )) {
            1
        } else {
            0
        }

        val (riskLevel, isValidSwell) = ClinicalScoringVariables.calculateRisk(thermalExceed, pressureExceed)

        // ── Thermal rule: 2-day sustained clean daily hotspot (Armstrong/IWGDF) ──
        // Only evaluate bilateral thermal asymmetry if BOTH feet are connected
        if (hasBothFeetForThermal) {
            if (clinical != null) {
                // Paper-based evaluation: requires 2 consecutive days of clean daily mean ΔT >= 2.2°C
                if (clinical.isSustainedHotspot2Day && clinical.sustainedThermalSide != null) {
                    val foot = clinical.sustainedThermalSide
                    val key = "THERMAL_$foot"
                    if (cooldownElapsed(key, config.thermalCooldownMinutes * 60_000L)) {
                        lastShownAtMs[key] = clock.currentTimeMillis()
                        prompts += DomainPrompt(key, PromptSeverity.ATTENTION, foot, config.version, riskLevel = riskLevel, isValidSwell = isValidSwell)
                    }
                }
                // Note: If consecutiveThermalDays == 1, it is Day 1 observation: do not fire alert
            } else if (data.leftTemperature > 0f && data.rightTemperature > 0f) {
                // Legacy / fallback gate for raw stream persistence
                val diff = abs(data.leftTemperature - data.rightTemperature)
                if (diff >= config.thermalDeltaWarningC) {
                    val foot = if (data.leftTemperature > data.rightTemperature) FootSide.LEFT else FootSide.RIGHT
                    val key = "THERMAL_$foot"
                    val count = (consecutiveCounts[key] ?: 0) + 1
                    consecutiveCounts[key] = count
                    // Persistence must be consecutive on the SAME foot — a flip
                    // of the warmer foot restarts the other side's count.
                    val oppositeKey = "THERMAL_${if (foot == FootSide.LEFT) FootSide.RIGHT else FootSide.LEFT}"
                    consecutiveCounts[oppositeKey] = 0

                    if (count >= config.thermalPersistenceCount &&
                        cooldownElapsed(key, config.thermalCooldownMinutes * 60_000L)
                    ) {
                        lastShownAtMs[key] = clock.currentTimeMillis()
                        prompts += DomainPrompt(key, PromptSeverity.ATTENTION, foot, config.version, riskLevel = riskLevel, isValidSwell = isValidSwell)
                    }
                } else {
                    consecutiveCounts["THERMAL_${FootSide.LEFT}"] = 0
                    consecutiveCounts["THERMAL_${FootSide.RIGHT}"] = 0
                }
            }
        } else {
            consecutiveCounts["THERMAL_${FootSide.LEFT}"] = 0
            consecutiveCounts["THERMAL_${FootSide.RIGHT}"] = 0
        }

        // ── Pressure rule: threshold + persistence gate (Rule R2) + cooldown per foot ──
        // Evaluates independently per connected foot
        if (input.leftConnected && isStanceOrUnspecified && data.leftPressure >= effectiveWarning) {
            val key = "PRESSURE_${FootSide.LEFT}"
            val count = (consecutiveCounts[key] ?: 0) + 1
            consecutiveCounts[key] = count
            if (count >= config.pressurePersistenceCount &&
                cooldownElapsed(key, config.pressureCooldownMinutes * 60_000L)
            ) {
                lastShownAtMs[key] = clock.currentTimeMillis()
                prompts += DomainPrompt(key, PromptSeverity.ATTENTION, FootSide.LEFT, config.version, riskLevel = riskLevel, isValidSwell = isValidSwell)
            }
        } else {
            consecutiveCounts["PRESSURE_${FootSide.LEFT}"] = 0
        }

        if (input.rightConnected && isStanceOrUnspecified && data.rightPressure >= effectiveWarning) {
            val key = "PRESSURE_${FootSide.RIGHT}"
            val count = (consecutiveCounts[key] ?: 0) + 1
            consecutiveCounts[key] = count
            if (count >= config.pressurePersistenceCount &&
                cooldownElapsed(key, config.pressureCooldownMinutes * 60_000L)
            ) {
                lastShownAtMs[key] = clock.currentTimeMillis()
                prompts += DomainPrompt(key, PromptSeverity.ATTENTION, FootSide.RIGHT, config.version, riskLevel = riskLevel, isValidSwell = isValidSwell)
            }
        } else {
            consecutiveCounts["PRESSURE_${FootSide.RIGHT}"] = 0
        }

        // Rule R5: "No recent data is never shown as All Clear."
        // If prompts fired, ATTENTION. If no prompts and one foot is missing, NO_DATA. Otherwise ALL_CLEAR.
        val status = when {
            prompts.isNotEmpty() -> OverallStatus.ATTENTION
            !hasBothFeetForThermal -> OverallStatus.NO_DATA
            else -> OverallStatus.ALL_CLEAR
        }
        val finalPrompts = if (status == OverallStatus.NO_DATA && prompts.isEmpty()) {
            listOf(DomainPrompt("NO_DATA", PromptSeverity.INFO, null, config.version))
        } else {
            prompts
        }
        return PromptEvaluation(status, finalPrompts)
    }

    private fun cooldownElapsed(key: String, cooldownMs: Long): Boolean {
        val last = lastShownAtMs[key] ?: return true
        return clock.currentTimeMillis() - last >= cooldownMs
    }
}
