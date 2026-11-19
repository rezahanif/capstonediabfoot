package com.project.insole.features.trends.presentation

import com.project.insole.core.database.dao.DailyFootMetricDao
import com.project.insole.core.database.dao.StepDao
import com.project.insole.core.database.entity.DailyFootMetricEntity
import com.project.insole.core.database.entity.StepEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TrendsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val stepMap = mutableMapOf<String, StepEntity>()
    private val metricMap = mutableMapOf<String, DailyFootMetricEntity>()

    private val fakeStepDao = object : StepDao {
        override suspend fun getStepsForDate(date: String): StepEntity? = stepMap[date]
        override fun getStepsFlowForDate(date: String): Flow<StepEntity?> = flowOf(stepMap[date])
        override suspend fun insertOrUpdateSteps(stepEntity: StepEntity) { stepMap[stepEntity.date] = stepEntity }
        override suspend fun deleteOldSteps(limit: Int) {}
    }

    private val fakeDailyFootMetricDao = object : DailyFootMetricDao {
        override suspend fun getMetricsForDate(date: String): DailyFootMetricEntity? = metricMap[date]
        override fun getMetricsFlowForDate(date: String): Flow<DailyFootMetricEntity?> = flowOf(metricMap[date])
        override suspend fun getMetricsSince(startDate: String): List<DailyFootMetricEntity> = metricMap.values.filter { it.date >= startDate }
        override fun getMetricsFlowSince(startDate: String): Flow<List<DailyFootMetricEntity>> = flowOf(metricMap.values.filter { it.date >= startDate })
        override suspend fun insertOrUpdateMetrics(entity: DailyFootMetricEntity) { metricMap[entity.date] = entity }
        override suspend fun deleteMetricsForDate(date: String) { metricMap.remove(date) }
        override suspend fun clearAllMetrics() { metricMap.clear() }
        override suspend fun deleteOldMetrics(limit: Int) {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        stepMap.clear()
        metricMap.clear()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty database produces honest empty trends state`() = runTest(testDispatcher) {
        val viewModel = TrendsViewModel(fakeStepDao, fakeDailyFootMetricDao)
        testScheduler.advanceUntilIdle()

        val state = viewModel.trendsState.value
        assertEquals(0, state.totalSteps)
        assertEquals(0, state.activeDays)
        assertEquals(ThermalStatus.NO_DATA, state.overallThermalStatus)
        assertEquals(0, state.totalWearMinutes)
        assertEquals(0, state.daysWithWearCount)
    }

    @Test
    fun `balanced metrics yield BALANCED overall thermal status`() = runTest(testDispatcher) {
        val today = LocalDate.now().toString()
        metricMap[today] = DailyFootMetricEntity(
            date = today,
            leftSumTemp = 320.0,
            leftSamples = 10,
            rightSumTemp = 322.0,
            rightSamples = 10,
            wearMinutes = 120
        )
        stepMap[today] = StepEntity(date = today, count = 1500)

        val viewModel = TrendsViewModel(fakeStepDao, fakeDailyFootMetricDao)
        testScheduler.advanceUntilIdle()

        val state = viewModel.trendsState.value
        assertEquals(1500, state.totalSteps)
        assertEquals(1, state.activeDays)
        assertEquals(ThermalStatus.BALANCED, state.overallThermalStatus)
        assertEquals(120, state.totalWearMinutes)
        assertEquals(1, state.daysWithWearCount)
        assertEquals(120, state.averageWearMinutesPerDay)
    }

    @Test
    fun `elevated asymmetry yields ATTENTION overall thermal status`() = runTest(testDispatcher) {
        val today = LocalDate.now().toString()
        metricMap[today] = DailyFootMetricEntity(
            date = today,
            leftSumTemp = 350.0,
            leftSamples = 10,  // 35.0°C
            rightSumTemp = 310.0,
            rightSamples = 10,  // 31.0°C -> asymmetry = 4.0°C >= 2.2°C
            wearMinutes = 60
        )

        val viewModel = TrendsViewModel(fakeStepDao, fakeDailyFootMetricDao)
        testScheduler.advanceUntilIdle()

        val state = viewModel.trendsState.value
        assertEquals(ThermalStatus.ATTENTION, state.overallThermalStatus)
    }
}
