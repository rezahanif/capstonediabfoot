package com.project.insole.features.diagnostics.presentation.seeder

import com.project.insole.core.database.dao.DailyFootMetricDao
import com.project.insole.core.database.entity.DailyFootMetricEntity
import com.project.insole.features.notifications.domain.FootSide
import com.project.insole.features.sensor.data.datasource.BleSensorDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

fun interface YesterdayMetricsReloader {
    fun reloadYesterdayMetrics()
}

/**
 * Dedicated scenario seeder for engineering and clinical verification.
 * Prepares historical baseline states (e.g. yesterday's 2.2°C+ hotspot or normal baseline)
 * in the Room database so multi-day clinical logic (such as the 2-day Armstrong hotspot rule)
 * can be tested on-device without waiting 24–48 hours.
 */
@Singleton
class ClinicalScenarioSeeder @Inject constructor(
    private val dailyFootMetricDao: DailyFootMetricDao,
    private val metricsReloader: YesterdayMetricsReloader
) {

    fun getYesterdayDate(): String = LocalDate.now().minusDays(1).toString()

    fun getTodayDate(): String = LocalDate.now().toString()

    fun getYesterdayRecordFlow(): Flow<DailyFootMetricEntity?> {
        return dailyFootMetricDao.getMetricsFlowForDate(getYesterdayDate())
    }

    suspend fun getYesterdayRecord(): DailyFootMetricEntity? {
        return withContext(Dispatchers.IO) {
            dailyFootMetricDao.getMetricsForDate(getYesterdayDate())
        }
    }

    /**
     * Seeds yesterday's record with a sustained inflammatory hotspot.
     * When today's insole is streamed with elevated temperature on the same foot,
     * the app will immediately satisfy consecutiveThermalDays >= 2 and trigger
     * a paper-based sustained hotspot or valid swell alert.
     */
    suspend fun seedYesterdayHotspot(
        foot: FootSide = FootSide.LEFT,
        asymmetryDeltaC: Float = 2.5f,
        baselineTempC: Float = 31.0f
    ) = withContext(Dispatchers.IO) {
        val warmerTemp = baselineTempC + asymmetryDeltaC
        val leftAvg = if (foot == FootSide.LEFT) warmerTemp else baselineTempC
        val rightAvg = if (foot == FootSide.RIGHT) warmerTemp else baselineTempC

        val entity = DailyFootMetricEntity(
            date = getYesterdayDate(),
            leftMinTemp = leftAvg - 0.5f,
            leftMaxTemp = leftAvg + 0.5f,
            leftSumTemp = leftAvg.toDouble() * 120,
            leftSamples = 120,
            rightMinTemp = rightAvg - 0.5f,
            rightMaxTemp = rightAvg + 0.5f,
            rightSumTemp = rightAvg.toDouble() * 120,
            rightSamples = 120,
            leftPeakPressureKpa = 95f,
            rightPeakPressureKpa = 95f,
            wearMinutes = 180,
            cleanLeftMinTemp = leftAvg - 0.4f,
            cleanLeftMaxTemp = leftAvg + 0.4f,
            cleanLeftSumTemp = leftAvg.toDouble() * 120,
            cleanLeftSamples = 120,
            cleanRightMinTemp = rightAvg - 0.4f,
            cleanRightMaxTemp = rightAvg + 0.4f,
            cleanRightSumTemp = rightAvg.toDouble() * 120,
            cleanRightSamples = 120,
            cleanWearMinutes = 180,
            cleanStancePressureSumKpa = 80.0 * 60,
            cleanStanceSamples = 60,
            consecutiveThermalDays = 1,
            sustainedThermalSide = foot.name
        )

        dailyFootMetricDao.insertOrUpdateMetrics(entity)
        metricsReloader.reloadYesterdayMetrics()
    }

    /**
     * Seeds yesterday's record as a completely normal baseline (ΔT = 0.2°C, wear = 180 min).
     */
    suspend fun seedYesterdayBaseline() = withContext(Dispatchers.IO) {
        val entity = DailyFootMetricEntity(
            date = getYesterdayDate(),
            leftMinTemp = 31.0f,
            leftMaxTemp = 32.0f,
            leftSumTemp = 31.4 * 120,
            leftSamples = 120,
            rightMinTemp = 31.0f,
            rightMaxTemp = 31.8f,
            rightSumTemp = 31.2 * 120,
            rightSamples = 120,
            leftPeakPressureKpa = 85f,
            rightPeakPressureKpa = 90f,
            wearMinutes = 200,
            cleanLeftMinTemp = 31.0f,
            cleanLeftMaxTemp = 32.0f,
            cleanLeftSumTemp = 31.4 * 120,
            cleanLeftSamples = 120,
            cleanRightMinTemp = 31.0f,
            cleanRightMaxTemp = 31.8f,
            cleanRightSumTemp = 31.2 * 120,
            cleanRightSamples = 120,
            cleanWearMinutes = 200,
            cleanStancePressureSumKpa = 75.0 * 60,
            cleanStanceSamples = 60,
            consecutiveThermalDays = 0,
            sustainedThermalSide = null
        )

        dailyFootMetricDao.insertOrUpdateMetrics(entity)
        metricsReloader.reloadYesterdayMetrics()
    }

    /**
     * Clears yesterday's seeded metric from the Room database.
     */
    suspend fun clearYesterdayRecord() = withContext(Dispatchers.IO) {
        dailyFootMetricDao.deleteMetricsForDate(getYesterdayDate())
        metricsReloader.reloadYesterdayMetrics()
    }
}
