package com.project.insole.features.trends.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.core.database.dao.DailyFootMetricDao
import com.project.insole.core.database.dao.StepDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

/** One day of real activity from the local step history. */
data class DayActivity(
    val dayLabel: String,   // "Today", "Mon", "Tue"…
    val steps: Int,
    val isToday: Boolean
)

/** Clinical thermal balance status for a day or week (calm patient-facing category, Rule R1). */
enum class ThermalStatus {
    NO_DATA,         // Not worn or insufficient bilateral readings
    BALANCED,        // Normal bilateral balance (< 1.5°C asymmetry)
    MILD_WARMTH,     // Mild warmth noticed (1.5°C - 2.2°C asymmetry)
    ATTENTION        // Elevated asymmetry (>= 2.2°C)
}

data class DayThermalMetric(
    val dayLabel: String,
    val status: ThermalStatus,
    val wearMinutes: Int,
    val isToday: Boolean
)

data class TrendsUiState(
    val isLoading: Boolean = false,
    val week: List<DayActivity> = emptyList(),   // oldest → newest (Today last)
    val totalSteps: Int = 0,
    val activeDays: Int = 0,
    val mostActiveDay: DayActivity? = null,
    // TD-7: Real Thermal Balance & Wear Time Adherence
    val thermalWeek: List<DayThermalMetric> = emptyList(),
    val overallThermalStatus: ThermalStatus = ThermalStatus.NO_DATA,
    val totalWearMinutes: Int = 0,
    val averageWearMinutesPerDay: Int = 0,
    val daysWithWearCount: Int = 0,
    val errorMessage: String? = null
)

/**
 * Trends tab — real activity, thermal balance, and wear compliance
 * from local Room history (StepDao & DailyFootMetricDao).
 *
 * Adheres strictly to UX-BRAND-SPEC R1 (no raw temperatures to patients),
 * R5 (no fake "All Clear" on empty data), and R7 (only real local measurements).
 */
@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val stepDao: StepDao,
    private val dailyFootMetricDao: DailyFootMetricDao
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _trendsState = MutableStateFlow(TrendsUiState())
    val trendsState: StateFlow<TrendsUiState> = _trendsState

    init {
        loadTrends()
    }

    fun loadTrends() {
        viewModelScope.launch {
            _trendsState.value = _trendsState.value.copy(isLoading = true)
            try {
                val today = LocalDate.now()
                val stepDays = mutableListOf<DayActivity>()
                val thermalDays = mutableListOf<DayThermalMetric>()

                (6 downTo 0).forEach { offset ->
                    val date = today.minusDays(offset.toLong())
                    val dateStr = date.format(dateFormatter)
                    val isToday = offset == 0
                    val dayLabel = if (isToday) "Today"
                    else date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

                    // Real steps from Room
                    val stepEntity = stepDao.getStepsForDate(dateStr)
                    stepDays.add(
                        DayActivity(
                            dayLabel = dayLabel,
                            steps = stepEntity?.count ?: 0,
                            isToday = isToday
                        )
                    )

                    // Real clinical thermal & wear metric from Room (TD-7)
                    val metric = dailyFootMetricDao.getMetricsForDate(dateStr)
                    val status = when {
                        metric == null || metric.leftSamples == 0 || metric.rightSamples == 0 -> ThermalStatus.NO_DATA
                        metric.thermalAsymmetryC < 1.5f -> ThermalStatus.BALANCED
                        metric.thermalAsymmetryC < 2.2f -> ThermalStatus.MILD_WARMTH
                        else -> ThermalStatus.ATTENTION
                    }
                    thermalDays.add(
                        DayThermalMetric(
                            dayLabel = dayLabel,
                            status = status,
                            wearMinutes = metric?.wearMinutes ?: 0,
                            isToday = isToday
                        )
                    )
                }

                // Overall weekly thermal status (calm synthesis over bilateral days)
                val daysWithThermalData = thermalDays.filter { it.status != ThermalStatus.NO_DATA }
                val overallThermal = when {
                    daysWithThermalData.isEmpty() -> ThermalStatus.NO_DATA
                    daysWithThermalData.any { it.status == ThermalStatus.ATTENTION } -> ThermalStatus.ATTENTION
                    daysWithThermalData.any { it.status == ThermalStatus.MILD_WARMTH } -> ThermalStatus.MILD_WARMTH
                    else -> ThermalStatus.BALANCED
                }

                val totalWearMinutes = thermalDays.sumOf { it.wearMinutes }
                val daysWithWear = thermalDays.count { it.wearMinutes >= 15 }
                val avgWear = if (daysWithWear > 0) totalWearMinutes / daysWithWear else 0

                _trendsState.value = TrendsUiState(
                    isLoading = false,
                    week = stepDays,
                    totalSteps = stepDays.sumOf { it.steps },
                    activeDays = stepDays.count { it.steps > 0 },
                    mostActiveDay = stepDays.maxByOrNull { it.steps }?.takeIf { it.steps > 0 },
                    thermalWeek = thermalDays,
                    overallThermalStatus = overallThermal,
                    totalWearMinutes = totalWearMinutes,
                    averageWearMinutesPerDay = avgWear,
                    daysWithWearCount = daysWithWear
                )
            } catch (e: Exception) {
                _trendsState.value = _trendsState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}
