package com.project.insole.features.diagnostics

import com.project.insole.core.database.dao.DailyFootMetricDao
import com.project.insole.core.database.entity.DailyFootMetricEntity
import com.project.insole.features.diagnostics.presentation.seeder.ClinicalScenarioSeeder
import com.project.insole.features.diagnostics.presentation.seeder.YesterdayMetricsReloader
import com.project.insole.features.notifications.domain.FootSide
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ClinicalScenarioSeederTest {

    private val records = mutableMapOf<String, DailyFootMetricEntity>()
    private var reloadedCalled = false

    private val fakeDao = object : DailyFootMetricDao {
        override suspend fun getMetricsForDate(date: String): DailyFootMetricEntity? = records[date]
        override fun getMetricsFlowForDate(date: String): Flow<DailyFootMetricEntity?> = flowOf(records[date])
        override suspend fun getMetricsSince(startDate: String): List<DailyFootMetricEntity> = records.values.toList()
        override fun getMetricsFlowSince(startDate: String): Flow<List<DailyFootMetricEntity>> = flowOf(records.values.toList())
        override suspend fun insertOrUpdateMetrics(entity: DailyFootMetricEntity) {
            records[entity.date] = entity
        }
        override suspend fun deleteMetricsForDate(date: String) {
            records.remove(date)
        }
        override suspend fun clearAllMetrics() {
            records.clear()
        }
        override suspend fun deleteOldMetrics(limit: Int) {}
    }

    private val fakeReloader = YesterdayMetricsReloader {
        reloadedCalled = true
    }

    private lateinit var seeder: ClinicalScenarioSeeder

    @Before
    fun setUp() {
        records.clear()
        reloadedCalled = false
        seeder = ClinicalScenarioSeeder(fakeDao, fakeReloader)
    }

    @Test
    fun `seedYesterdayHotspot on LEFT inserts correct asymmetry and notifies dataSource`() = runBlocking {
        seeder.seedYesterdayHotspot(foot = FootSide.LEFT, asymmetryDeltaC = 2.5f)

        val yesterday = seeder.getYesterdayDate()
        val entity = records[yesterday]
        assertNotNull(entity)
        assertEquals("LEFT", entity?.sustainedThermalSide)
        assertEquals(1, entity?.consecutiveThermalDays)
        assertEquals(2.5f, entity!!.cleanThermalAsymmetryC, 0.05f)
        assertEquals(true, reloadedCalled)
    }

    @Test
    fun `seedYesterdayBaseline inserts balanced reading and notifies dataSource`() = runBlocking {
        seeder.seedYesterdayBaseline()

        val yesterday = seeder.getYesterdayDate()
        val entity = records[yesterday]
        assertNotNull(entity)
        assertEquals(0, entity?.consecutiveThermalDays)
        assertEquals(null, entity?.sustainedThermalSide)
        assertEquals(true, reloadedCalled)
    }

    @Test
    fun `clearYesterdayRecord removes the record from database`() = runBlocking {
        seeder.seedYesterdayHotspot(foot = FootSide.LEFT)
        val yesterday = seeder.getYesterdayDate()
        assertNotNull(records[yesterday])

        seeder.clearYesterdayRecord()
        assertEquals(null, records[yesterday])
        assertEquals(true, reloadedCalled)
    }
}
