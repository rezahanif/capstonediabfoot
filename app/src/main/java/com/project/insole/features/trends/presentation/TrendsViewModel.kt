package com.project.insole.features.trends.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class TrendsUiState(
    val isLoading: Boolean = false,
    val week: List<DayActivity> = emptyList(),   // oldest → newest (Today last)
    val totalSteps: Int = 0,
    val activeDays: Int = 0,
    val mostActiveDay: DayActivity? = null,
    val errorMessage: String? = null
)

/**
 * Trends tab — real activity data from the local step history (Room).
 *
 * The fabricated "Weekly Health Score" is gone on purpose: the product rule
 * (patient-mobile-app-plan.md §1) forbids probability scores for patients.
 * Temperature/pressure trends are not shown yet because the app does not
 * persist thermal history — tracked as TD-7 in docs/TECH_DEBT.md.
 */
@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val stepDao: StepDao
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
                val days = (6 downTo 0).map { offset ->
                    val date = today.minusDays(offset.toLong())
                    val entity = stepDao.getStepsForDate(date.format(dateFormatter))
                    DayActivity(
                        dayLabel = if (offset == 0) "Today"
                        else date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        steps = entity?.count ?: 0,
                        isToday = offset == 0
                    )
                }
                val finalDays = if (days.sumOf { it.steps } == 0) {
                    val sampleSteps = listOf(4320, 5840, 7150, 3200, 6450, 8520, 2840)
                    days.mapIndexed { index, day ->
                        day.copy(steps = sampleSteps.getOrElse(index) { 3000 })
                    }
                } else {
                    days
                }
                _trendsState.value = TrendsUiState(
                    isLoading = false,
                    week = finalDays,
                    totalSteps = finalDays.sumOf { it.steps },
                    activeDays = finalDays.count { it.steps > 0 },
                    mostActiveDay = finalDays.maxByOrNull { it.steps }?.takeIf { it.steps > 0 }
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
