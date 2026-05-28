package com.project.insole.features.trends.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.features.trends.data.TrendsDataSource
import com.project.insole.features.trends.data.DailySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrendsUiState(
    val isLoading: Boolean = false,
    val dailySummaries: List<DailySummary> = emptyList(),
    val weeklyHealthScore: Float = 0f,
    val selectedDate: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

/**
 * ViewModel for trends/analytics screen.
 * Shows medical summary by timeframe (daily, weekly, monthly).
 */
@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val trendsDataSource: TrendsDataSource
) : ViewModel() {

    private val _trendsState = MutableStateFlow(TrendsUiState())
    val trendsState: StateFlow<TrendsUiState> = _trendsState

    init {
        loadTrends()
    }

    fun loadTrends() {
        viewModelScope.launch {
            _trendsState.value = _trendsState.value.copy(isLoading = true)
            
            // Fetch daily summaries for last 7 days
            val dailyResults = (0..6).map { dayOffset ->
                val dateMs = System.currentTimeMillis() - (dayOffset * 24 * 60 * 60 * 1000)
                trendsDataSource.getDailySummary(dateMs)
            }

            // Fetch weekly health score
            val scoreResult = trendsDataSource.getWeeklyHealthScore()

            scoreResult.onSuccess { score ->
                val summaries = dailyResults.mapNotNull { it.getOrNull() }
                _trendsState.value = TrendsUiState(
                    isLoading = false,
                    dailySummaries = summaries,
                    weeklyHealthScore = score
                )
            }.onFailure { exception ->
                _trendsState.value = TrendsUiState(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    fun selectDate(dateMs: Long) {
        _trendsState.value = _trendsState.value.copy(selectedDate = dateMs)
    }
}
