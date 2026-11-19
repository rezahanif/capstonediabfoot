package com.project.insole.features.sensor.domain.usecase

import com.project.insole.core.database.dao.DailyFootMetricDao
import com.project.insole.core.database.dao.HourlyStepDao
import com.project.insole.core.database.dao.StepDao
import com.project.insole.core.database.entity.DailyFootMetricEntity
import com.project.insole.core.database.entity.HourlyStepEntity
import com.project.insole.core.database.entity.StepEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateCloudPayloadUseCaseTest {

    private val fakeMetricDao = object : DailyFootMetricDao {
        override suspend fun getMetricsForDate(date: String): DailyFootMetricEntity? {
            return if (date == "2026-10-25") {
                DailyFootMetricEntity(
                    date = date,
                    leftMinTemp = 30.1f,
                    leftMaxTemp = 33.8f,
                    leftSumTemp = 3220.0,
                    leftSamples = 100, // avg = 32.2°C
                    rightMinTemp = 29.0f,
                    rightMaxTemp = 31.0f,
                    rightSumTemp = 2950.0,
                    rightSamples = 100, // avg = 29.5°C -> asymmetry = 2.7°C >= 2.2°C
                    leftPeakPressureKpa = 220f,
                    rightPeakPressureKpa = 180f,
                    wearMinutes = 180
                )
            } else null
        }
        override fun getMetricsFlowForDate(date: String): Flow<DailyFootMetricEntity?> = flowOf(null)
        override suspend fun getMetricsSince(startDate: String): List<DailyFootMetricEntity> = emptyList()
        override fun getMetricsFlowSince(startDate: String): Flow<List<DailyFootMetricEntity>> = flowOf(emptyList())
        override suspend fun insertOrUpdateMetrics(entity: DailyFootMetricEntity) {}
        override suspend fun deleteMetricsForDate(date: String) {}
        override suspend fun clearAllMetrics() {}
        override suspend fun deleteOldMetrics(limit: Int) {}
    }

    private val fakeStepDao = object : StepDao {
        override suspend fun getStepsForDate(date: String): StepEntity? {
            return if (date == "2026-10-25") {
                StepEntity(date = date, count = 4200, leftCount = 2100, rightCount = 2100)
            } else null
        }
        override fun getStepsFlowForDate(date: String): Flow<StepEntity?> = flowOf(null)
        override suspend fun insertOrUpdateSteps(stepEntity: StepEntity) {}
        override suspend fun deleteOldSteps(limit: Int) {}
    }

    private val fakeHourlyDao = object : HourlyStepDao {
        override fun getHourlyStepsForDate(date: String): Flow<List<HourlyStepEntity>> {
            return flowOf(listOf(HourlyStepEntity(date = date, hour = 10, count = 800)))
        }
        override suspend fun getStepsForHour(date: String, hour: Int): HourlyStepEntity? = null
        override suspend fun insertOrUpdate(entity: HourlyStepEntity) {}
        override suspend fun incrementSteps(date: String, hour: Int, diff: Int): Int = 0
        override suspend fun deleteOldHourlySteps(daysLimit: Int) {}
    }

    private val useCase = GenerateCloudPayloadUseCase(
        dailyFootMetricDao = fakeMetricDao,
        stepDao = fakeStepDao,
        hourlyStepDao = fakeHourlyDao,
        deviceIdProvider = { "L:11:22:33:44:55:66+R:AA:BB:CC:DD:EE:FF" },
        patientIdProvider = { "PT-4AB79CD2" }
    )

    @Test
    fun `generateDailyEnvelope creates compliant DATA_CONTRACT envelope`() = runTest {
        val envelope = useCase.generateDailyEnvelope("2026-10-25")

        assertEquals("1.0", envelope.schemaVersion)
        assertEquals("PT-4AB79CD2", envelope.patientId)
        assertEquals("L:11:22:33:44:55:66+R:AA:BB:CC:DD:EE:FF", envelope.deviceId)
        assertEquals("none", envelope.calibrationVersion)

        val batch = envelope.batch
        assertEquals("2026-10-25T00:00:00Z", batch.period.start)
        assertEquals("2026-10-25T23:59:00Z", batch.period.end)
        assertEquals(180, batch.wearMinutes)

        // Activity
        assertEquals(4200, batch.dailyActivity.totalSteps)
        assertEquals(2100, batch.dailyActivity.leftSteps)
        assertEquals(2100, batch.dailyActivity.rightSteps)
        assertEquals(10, batch.dailyActivity.activeMinutesEstimated) // 800 steps / 80 = 10 mins

        // Temperature
        assertEquals("MIDFOOT", batch.temperature.left.siteId)
        assertEquals(30.1f, batch.temperature.left.minC, 1e-4f)
        assertEquals(33.8f, batch.temperature.left.maxC, 1e-4f)
        assertEquals(32.2f, batch.temperature.left.avgC, 1e-4f)

        // Clinical episodes: asymmetry 2.7°C >= 2.2°C and left pressure 220 >= 200
        val asymmetryEpisode = batch.episodes.find { it.type == "THERMAL_ASYMMETRY" }
        assertTrue(asymmetryEpisode != null)
        assertEquals("L", asymmetryEpisode?.foot)
        assertEquals("MIDFOOT", asymmetryEpisode?.siteOrRegion)
        assertEquals(2.7f, asymmetryEpisode?.peakValue ?: 0f, 1e-2f)

        val pressureEpisode = batch.episodes.find { it.type == "HIGH_PRESSURE" }
        assertTrue(pressureEpisode != null)
        assertEquals("L", pressureEpisode?.foot)
        assertEquals(220f, pressureEpisode?.peakValue ?: 0f, 1e-4f)
    }

    @Test
    fun `envelopeToJson produces valid json format with all required keys`() = runTest {
        val envelope = useCase.generateDailyEnvelope("2026-10-25")
        val json = useCase.envelopeToJson(envelope)

        assertTrue(json.contains("\"schema_version\": \"1.0\""))
        assertTrue(json.contains("\"patient_id\": \"PT-4AB79CD2\""))
        assertTrue(json.contains("\"site_id\": \"MIDFOOT\""))
        assertTrue(json.contains("\"wear_minutes\": 180"))
        assertTrue(json.contains("\"total_steps\": 4200"))
        assertTrue(json.contains("\"THERMAL_ASYMMETRY\""))
        assertTrue(json.contains("\"HIGH_PRESSURE\""))
        assertTrue(json.contains("\"risk_level\": \"HIGH\""))
        assertTrue(json.contains("\"is_valid_swell\": true"))
    }
}
