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
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
 * [PromptRuleEngine], and maps the semantic [DomainPrompt]s to the calm
 * patient-facing copy. Copy follows the product rule: name the foot and the
 * action, never show raw numbers, never use alarm language.
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

    // Stateful rule engine — fed only from the main dispatcher (this VM's
    // collectors), so its persistence counters and cooldowns stay consistent.
    private val ruleEngine = PromptRuleEngine()

    // Latest inputs, cached for re-evaluation whenever ANY input changes.
    private var latestSensorData: InsoleSensorData? = null
    private var leftConnected = false
    private var rightConnected = false

    init {
        observeLiveData()
        loadHistory()
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
    }

    private fun recompute() {
        val evaluation = ruleEngine.evaluate(
            RuleInput(leftConnected, rightConnected, latestSensorData)
        )
        _notificationsState.value = _notificationsState.value.copy(
            overallStatus = evaluation.overallStatus,
            prompts = evaluation.prompts.map { it.toUiPrompt() },
            leftConnected = leftConnected,
            rightConnected = rightConnected
        )
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
            GuidancePrompt(
                id = id,
                severity = severity,
                title = "Check your $foot foot",
                message = "Your $foot foot has been warmer than your $other foot for a while.",
                actions = listOf(
                    "Take a look for redness, swelling, or a sore",
                    "If you don't see anything, mention it at your next check-up"
                ),
                thresholdVersion = thresholdVersion
            )
        }
        else -> {
            val foot = footName(foot)
            GuidancePrompt(
                id = id,
                severity = severity,
                title = "Your $foot foot is taking more pressure",
                message = "Your $foot foot is working harder than usual right now.",
                actions = listOf(
                    "Slow down for a moment",
                    "Change your walking or resting position",
                    "Check your footwear"
                ),
                thresholdVersion = thresholdVersion
            )
        }
    }

    private fun footName(foot: FootSide?): String =
        if (foot == FootSide.RIGHT) "right" else "left"

    /** Push/prompt history — the data source is a scaffold until the backend
     *  exists (TD-4); the UI hides this section while it is empty. */
    private fun loadHistory() {
        viewModelScope.launch {
            val result = notificationDataSource.getNotificationHistory()
            result.onSuccess { notifications ->
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
}
