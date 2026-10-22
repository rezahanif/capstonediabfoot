package com.project.insole.features.notifications.domain

import com.project.insole.features.sensor.domain.model.Clock
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
    val thresholdVersion: String
)

data class RuleInput(
    val leftConnected: Boolean,
    val rightConnected: Boolean,
    val sensorData: InsoleSensorData?
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
        val prompts = mutableListOf<DomainPrompt>()

        // ── No-data state: honest, never disguised as stable ────────────
        if (!input.leftConnected || !input.rightConnected || input.sensorData == null) {
            return PromptEvaluation(
                overallStatus = OverallStatus.NO_DATA,
                prompts = listOf(
                    DomainPrompt("NO_DATA", PromptSeverity.INFO, null, config.version)
                )
            )
        }

        val data = input.sensorData

        // ── Thermal rule: persistence gate + cooldown ────────────────────
        if (data.leftTemperature > 0f && data.rightTemperature > 0f) {
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
                    prompts += DomainPrompt(key, PromptSeverity.ATTENTION, foot, config.version)
                }
            } else {
                consecutiveCounts["THERMAL_${FootSide.LEFT}"] = 0
                consecutiveCounts["THERMAL_${FootSide.RIGHT}"] = 0
            }
        }

        // ── Pressure rule: threshold + cooldown per foot ─────────────────
        if (data.leftPressure >= config.pressureWarning &&
            cooldownElapsed("PRESSURE_${FootSide.LEFT}", config.pressureCooldownMinutes * 60_000L)
        ) {
            lastShownAtMs["PRESSURE_${FootSide.LEFT}"] = clock.currentTimeMillis()
            prompts += DomainPrompt("PRESSURE_${FootSide.LEFT}", PromptSeverity.ATTENTION, FootSide.LEFT, config.version)
        }
        if (data.rightPressure >= config.pressureWarning &&
            cooldownElapsed("PRESSURE_${FootSide.RIGHT}", config.pressureCooldownMinutes * 60_000L)
        ) {
            lastShownAtMs["PRESSURE_${FootSide.RIGHT}"] = clock.currentTimeMillis()
            prompts += DomainPrompt("PRESSURE_${FootSide.RIGHT}", PromptSeverity.ATTENTION, FootSide.RIGHT, config.version)
        }

        val status = if (prompts.isNotEmpty()) OverallStatus.ATTENTION else OverallStatus.ALL_CLEAR
        return PromptEvaluation(status, prompts)
    }

    private fun cooldownElapsed(key: String, cooldownMs: Long): Boolean {
        val last = lastShownAtMs[key] ?: return true
        return clock.currentTimeMillis() - last >= cooldownMs
    }
}
