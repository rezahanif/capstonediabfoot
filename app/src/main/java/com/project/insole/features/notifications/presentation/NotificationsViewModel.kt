package com.project.insole.features.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.core.ble.InsoleBleManager
import com.project.insole.features.notifications.data.NotificationDataSource
import com.project.insole.features.notifications.domain.DomainPrompt
import com.project.insole.features.notifications.domain.FootSide
import com.project.insole.features.notifications.domain.NotificationSummary
import com.project.insole.features.notifications.domain.PromptRuleEngine
import com.project.insole.features.notifications.domain.PromptSeverity
import com.project.insole.features.notifications.domain.OverallStatus
import com.project.insole.features.notifications.domain.RuleInput
import com.project.insole.features.notifications.domain.ThresholdsConfigRepository
import com.project.insole.features.sensor.domain.model.ClinicalScoringVariables
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.model.RiskLevel
import com.project.insole.features.sensor.domain.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import javax.inject.Inject

data class GuidancePrompt(
    val id: String,                 // stable key: THERMAL_LEFT, PRESSURE_R, NO_DATA…
    val severity: PromptSeverity,
    val title: String,
    val message: String,
    val actions: List<String>,      // "what you can do right now" bullets
    /** Rule regime that produced this prompt (flows into DATA_CONTRACT
     *  prompt_log / episodes when the sync module lands). */
    val thresholdVersion: String
)

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val overallStatus: OverallStatus = OverallStatus.NO_DATA,
    val prompts: List<GuidancePrompt> = emptyList(),
    val leftConnected: Boolean = false,
    val rightConnected: Boolean = false,
    val history: List<NotificationSummary> = emptyList(),
    val errorMessage: String? = null
)

/**
 * Alerts tab — thin presentation layer over the domain rule engine (TD-8).
 *
 * The ViewModel owns NO rules: it collects live inputs (connection states +
 * sensor data + the versioned thresholds config), feeds them to
 * [PromptRuleEngine], and maps the semantic [DomainPrompt]s to calm
 * patient-facing copy. Copy follows the product rule: name the foot and the
 * action, never show raw numbers, never use alarm language.
 *
 * Real alert occurrences are recorded in Room via [NotificationDataSource].
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val bleManager: InsoleBleManager,
    private val sensorRepository: SensorRepository,
    private val notificationDataSource: NotificationDataSource,
    private val thresholdsConfigRepository: ThresholdsConfigRepository
) : ViewModel() {

    private val _notificationsState = MutableStateFlow(NotificationsUiState())
    val notificationsState: StateFlow<NotificationsUiState> = _notificationsState

    companion object {
        /** 3-second buffer to hold state and eliminate high-frequency UI jitter during walking swing or initial sync. */
        private const val BUFFER_HOLD_DURATION_MS = 3_000L
    }

    // Stateful rule engine — fed only from the main dispatcher (this VM's
    // collectors), so its persistence counters and cooldowns stay consistent.
    private val ruleEngine = PromptRuleEngine()

    // Active prompts displayed while conditions persist
    private val activeAttentionPrompts = mutableMapOf<String, GuidancePrompt>()
    private val promptHoldUntilMs = mutableMapOf<String, Long>()
    private var holdExpiryJob: Job? = null

    // Latest inputs, cached for re-evaluation whenever ANY input changes.
    private var latestSensorData: InsoleSensorData? = null
    private var latestClinicalVariables: ClinicalScoringVariables? = null
    private var leftConnected = false
    private var rightConnected = false

    init {
        observeLiveData()
        observeHistory()
        viewModelScope.launch {
            thresholdsConfigRepository.refresh()
        }
    }

    private fun observeLiveData() {
        viewModelScope.launch {
            thresholdsConfigRepository.config.collect { config ->
                ruleEngine.updateConfig(config)
                recompute()
            }
        }
        viewModelScope.launch {
            bleManager.leftDeviceState.collect { state ->
                leftConnected = state == com.project.insole.core.ble.model.BleDeviceState.Connected
                recompute()
            }
        }
        viewModelScope.launch {
            bleManager.rightDeviceState.collect { state ->
                rightConnected = state == com.project.insole.core.ble.model.BleDeviceState.Connected
                recompute()
            }
        }
        viewModelScope.launch {
            sensorRepository.getSensorDataFlow().collect { data ->
                latestSensorData = data
                recompute()
            }
        }
        viewModelScope.launch {
            sensorRepository.getClinicalScoringVariablesFlow().collect { clinical ->
                latestClinicalVariables = clinical
                recompute()
            }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            notificationDataSource.getNotificationsFlow().collect { notifications ->
                val summaries = notifications.map {
                    NotificationSummary(
                        id = it.id,
                        title = it.title,
                        message = it.message,
                        type = it.severity,
                        timestamp = it.timestamp,
                        read = it.read
                    )
                }
                _notificationsState.value = _notificationsState.value.copy(history = summaries)
            }
        }
    }

    private fun recompute() {
        val now = System.currentTimeMillis()
        val isConnected = leftConnected || rightConnected
        val data = latestSensorData

        if (!isConnected || data == null) {
            // Disconnected / no data state
            activeAttentionPrompts.clear()
            promptHoldUntilMs.clear()
            holdExpiryJob?.cancel()
            val noDataPrompt = DomainPrompt(
                id = "NO_DATA",
                severity = PromptSeverity.INFO,
                foot = null,
                thresholdVersion = ruleEngine.config.version
            ).toUiPrompt()

            val newState = _notificationsState.value.copy(
                overallStatus = OverallStatus.NO_DATA,
                prompts = listOf(noDataPrompt),
                leftConnected = leftConnected,
                rightConnected = rightConnected
            )
            if (newState != _notificationsState.value) {
                _notificationsState.value = newState
            }
            return
        }

        val evaluation = ruleEngine.evaluate(
            RuleInput(leftConnected, rightConnected, latestSensorData, latestClinicalVariables)
        )

        // ── 1. Process newly fired attention prompts from the rule engine ─────
        evaluation.prompts
            .filter { it.severity == PromptSeverity.ATTENTION }
            .forEach { domainPrompt ->
                val isNewPrompt = !activeAttentionPrompts.containsKey(domainPrompt.id)
                val uiPrompt = domainPrompt.toUiPrompt()
                activeAttentionPrompts[domainPrompt.id] = uiPrompt
                promptHoldUntilMs[domainPrompt.id] = now + BUFFER_HOLD_DURATION_MS

                // Persist the alert to Room database ONLY ONCE per occurrence
                if (isNewPrompt) {
                    viewModelScope.launch {
                        notificationDataSource.saveNotification(
                            promptId = domainPrompt.id,
                            title = uiPrompt.title,
                            message = uiPrompt.message,
                            severity = domainPrompt.severity.name,
                            foot = domainPrompt.foot?.name,
                            thresholdVersion = domainPrompt.thresholdVersion
                        )
                    }
                }
            }

        // ── 2. Refresh hold timestamp while conditions actively persist ───────
        val effectivePressureWarning = ruleEngine.config.effectivePressureWarning()
        if (data.leftPressure >= effectivePressureWarning && activeAttentionPrompts.containsKey("PRESSURE_LEFT")) {
            promptHoldUntilMs["PRESSURE_LEFT"] = now + BUFFER_HOLD_DURATION_MS
        }
        if (data.rightPressure >= effectivePressureWarning && activeAttentionPrompts.containsKey("PRESSURE_RIGHT")) {
            promptHoldUntilMs["PRESSURE_RIGHT"] = now + BUFFER_HOLD_DURATION_MS
        }

        val clinical = latestClinicalVariables
        if (clinical != null && clinical.isSustainedHotspot2Day && clinical.sustainedThermalSide != null) {
            val key = "THERMAL_${clinical.sustainedThermalSide}"
            if (activeAttentionPrompts.containsKey(key)) {
                promptHoldUntilMs[key] = now + BUFFER_HOLD_DURATION_MS
            }
        } else if (data.leftTemperature > 0f && data.rightTemperature > 0f) {
            val diff = abs(data.leftTemperature - data.rightTemperature)
            if (diff >= ruleEngine.config.thermalDeltaWarningC) {
                if (activeAttentionPrompts.containsKey("THERMAL_LEFT")) {
                    promptHoldUntilMs["THERMAL_LEFT"] = now + BUFFER_HOLD_DURATION_MS
                }
                if (activeAttentionPrompts.containsKey("THERMAL_RIGHT")) {
                    promptHoldUntilMs["THERMAL_RIGHT"] = now + BUFFER_HOLD_DURATION_MS
                }
            }
        }

        // ── 3. Hold state buffer: only clear cards after 3s quiet window ──────
        if (data.leftPressure < effectivePressureWarning) {
            val holdUntil = promptHoldUntilMs["PRESSURE_LEFT"] ?: 0L
            if (now >= holdUntil) {
                activeAttentionPrompts.remove("PRESSURE_LEFT")
                promptHoldUntilMs.remove("PRESSURE_LEFT")
            }
        }
        if (data.rightPressure < effectivePressureWarning) {
            val holdUntil = promptHoldUntilMs["PRESSURE_RIGHT"] ?: 0L
            if (now >= holdUntil) {
                activeAttentionPrompts.remove("PRESSURE_RIGHT")
                promptHoldUntilMs.remove("PRESSURE_RIGHT")
            }
        }

        val isSustainedHot = clinical?.isSustainedHotspot2Day == true
        if (clinical != null) {
            if (!isSustainedHot) {
                val leftHoldUntil = promptHoldUntilMs["THERMAL_LEFT"] ?: 0L
                if (now >= leftHoldUntil) {
                    activeAttentionPrompts.remove("THERMAL_LEFT")
                    promptHoldUntilMs.remove("THERMAL_LEFT")
                }
                val rightHoldUntil = promptHoldUntilMs["THERMAL_RIGHT"] ?: 0L
                if (now >= rightHoldUntil) {
                    activeAttentionPrompts.remove("THERMAL_RIGHT")
                    promptHoldUntilMs.remove("THERMAL_RIGHT")
                }
            }
        } else if (data.leftTemperature > 0f && data.rightTemperature > 0f) {
            val diff = abs(data.leftTemperature - data.rightTemperature)
            if (diff < ruleEngine.config.thermalDeltaWarningC) {
                val leftHoldUntil = promptHoldUntilMs["THERMAL_LEFT"] ?: 0L
                if (now >= leftHoldUntil) {
                    activeAttentionPrompts.remove("THERMAL_LEFT")
                    promptHoldUntilMs.remove("THERMAL_LEFT")
                }
                val rightHoldUntil = promptHoldUntilMs["THERMAL_RIGHT"] ?: 0L
                if (now >= rightHoldUntil) {
                    activeAttentionPrompts.remove("THERMAL_RIGHT")
                    promptHoldUntilMs.remove("THERMAL_RIGHT")
                }
            }
        }

        // Schedule delayed check if any prompts are latched
        if (activeAttentionPrompts.isNotEmpty()) {
            scheduleHoldCheck()
        }

        val activePromptsList = activeAttentionPrompts.values.toList()
        val overall = if (activePromptsList.isNotEmpty()) {
            OverallStatus.ATTENTION
        } else {
            OverallStatus.ALL_CLEAR
        }

        val newState = _notificationsState.value.copy(
            overallStatus = overall,
            prompts = activePromptsList,
            leftConnected = leftConnected,
            rightConnected = rightConnected
        )
        if (newState != _notificationsState.value) {
            _notificationsState.value = newState
        }
    }

    private fun scheduleHoldCheck() {
        holdExpiryJob?.cancel()
        holdExpiryJob = viewModelScope.launch {
            delay(BUFFER_HOLD_DURATION_MS + 50L)
            recompute()
        }
    }

    // ── copy mapping: semantic prompt → calm patient-facing wording ─────

    private fun DomainPrompt.toUiPrompt(): GuidancePrompt = when {
        id == "NO_DATA" -> GuidancePrompt(
            id = id,
            severity = severity,
            title = "No recent data",
            message = "We haven't heard from your insoles recently. " +
                "Once they're connected, we'll quietly keep an eye on your feet.",
            actions = listOf(
                "Put on both insoles and open the app",
                "Check the pairing screen if they don't reconnect on their own"
            ),
            thresholdVersion = thresholdVersion
        )
        id.startsWith("THERMAL") -> {
            val side = foot ?: FootSide.LEFT
            val foot = footName(side)
            val other = if (side == FootSide.LEFT) "right" else "left"

            val message = if (isValidSwell) {
                "Your $foot foot has shown sustained warmth and elevated load. Please consider scheduling a visit with your doctor for a professional foot check."
            } else if (riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.MEDIUM) {
                "Your $foot foot has been warmer than your $other foot for a while. We recommend contacting your doctor or mentioning this at your next visit."
            } else {
                "Your $foot foot has been warmer than your $other foot for a while."
            }

            val actions = mutableListOf(
                "Take a look for redness, swelling, or a sore",
                "Rest your feet and avoid prolonged standing"
            )
            if (riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.MEDIUM) {
                actions.add("Contact your doctor or podiatrist for an evaluation")
            } else {
                actions.add("If you don't see anything, mention it at your next check-up")
            }

            GuidancePrompt(
                id = id,
                severity = severity,
                title = "Check your $foot foot",
                message = message,
                actions = actions,
                thresholdVersion = thresholdVersion
            )
        }
        else -> {
            val foot = footName(foot)
            val isMediumOrHigher = riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.MEDIUM

            val message = if (isMediumOrHigher) {
                "Your $foot foot has been taking more pressure. If uneven load or soreness continues, please consult your doctor."
            } else {
                "Your $foot foot is working harder than usual right now."
            }

            val actions = mutableListOf(
                "Slow down for a moment",
                "Change your walking or resting position",
                "Check your footwear"
            )
            if (isMediumOrHigher) {
                actions.add("Consult your doctor if discomfort or swelling develops")
            }

            GuidancePrompt(
                id = id,
                severity = severity,
                title = "Your $foot foot is taking more pressure",
                message = message,
                actions = actions,
                thresholdVersion = thresholdVersion
            )
        }
    }

    private fun footName(foot: FootSide?): String =
        if (foot == FootSide.RIGHT) "right" else "left"
}
