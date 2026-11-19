package com.project.insole.core.database.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyFootMetricEntityTest {

    @Test
    fun zeroSamples_returnsZeroAveragesAndAsymmetry() {
        val entity = DailyFootMetricEntity(date = "2026-10-25")
        assertEquals(0f, entity.leftAvgTemp, 0.001f)
        assertEquals(0f, entity.rightAvgTemp, 0.001f)
        assertEquals(0f, entity.thermalAsymmetryC, 0.001f)
        assertEquals(0f, entity.cleanLeftAvgTemp, 0.001f)
        assertEquals(0f, entity.cleanRightAvgTemp, 0.001f)
        assertEquals(0f, entity.cleanThermalAsymmetryC, 0.001f)
        assertEquals(0f, entity.cleanStanceMeanPressureKpa, 0.001f)
        assertFalse(entity.isSustainedHotspot2Day)
    }

    @Test
    fun singleFootSamples_returnsZeroAsymmetry() {
        val entity = DailyFootMetricEntity(
            date = "2026-10-25",
            leftSumTemp = 96.0,
            leftSamples = 3,
            rightSumTemp = 0.0,
            rightSamples = 0,
            cleanLeftSumTemp = 96.0,
            cleanLeftSamples = 3
        )
        assertEquals(32.0f, entity.leftAvgTemp, 0.001f)
        assertEquals(0f, entity.rightAvgTemp, 0.001f)
        assertEquals(0f, entity.thermalAsymmetryC, 0.001f)
        assertEquals(32.0f, entity.cleanLeftAvgTemp, 0.001f)
        assertEquals(0f, entity.cleanRightAvgTemp, 0.001f)
        assertEquals(0f, entity.cleanThermalAsymmetryC, 0.001f)
    }

    @Test
    fun bilateralSamples_computesAccurateAsymmetry() {
        val entity = DailyFootMetricEntity(
            date = "2026-10-25",
            leftSumTemp = 96.0,
            leftSamples = 3,  // Avg: 32.0°C
            rightSumTemp = 68.4,
            rightSamples = 2,  // Avg: 34.2°C
            cleanLeftSumTemp = 96.0,
            cleanLeftSamples = 3,
            cleanRightSumTemp = 68.4,
            cleanRightSamples = 2
        )
        assertEquals(32.0f, entity.leftAvgTemp, 0.001f)
        assertEquals(34.2f, entity.rightAvgTemp, 0.001f)
        assertEquals(2.2f, entity.thermalAsymmetryC, 0.001f)
        assertEquals(32.0f, entity.cleanLeftAvgTemp, 0.001f)
        assertEquals(34.2f, entity.cleanRightAvgTemp, 0.001f)
        assertEquals(2.2f, entity.cleanThermalAsymmetryC, 0.001f)
    }

    @Test
    fun wearMinutesAndPeakPressure_storedAccurately() {
        val entity = DailyFootMetricEntity(
            date = "2026-10-25",
            leftPeakPressureKpa = 85.5f,
            rightPeakPressureKpa = 92.0f,
            wearMinutes = 185,
            cleanWearMinutes = 170,
            cleanStancePressureSumKpa = 2400.0,
            cleanStanceSamples = 40
        )
        assertEquals(85.5f, entity.leftPeakPressureKpa, 0.001f)
        assertEquals(92.0f, entity.rightPeakPressureKpa, 0.001f)
        assertEquals(185, entity.wearMinutes)
        assertEquals(170, entity.cleanWearMinutes)
        assertEquals(60.0f, entity.cleanStanceMeanPressureKpa, 0.001f)
    }

    @Test
    fun consecutiveThermalDays_triggersSustainedHotspotOnlyOnDay2() {
        val day1Entity = DailyFootMetricEntity(
            date = "2026-10-25",
            consecutiveThermalDays = 1,
            sustainedThermalSide = "LEFT"
        )
        assertFalse("Day 1 is observation only, must not declare sustained hotspot", day1Entity.isSustainedHotspot2Day)

        val day2Entity = DailyFootMetricEntity(
            date = "2026-10-26",
            consecutiveThermalDays = 2,
            sustainedThermalSide = "LEFT"
        )
        assertTrue("Day 2 sustained on same foot must declare sustained hotspot", day2Entity.isSustainedHotspot2Day)

        val day2NoSideEntity = DailyFootMetricEntity(
            date = "2026-10-26",
            consecutiveThermalDays = 2,
            sustainedThermalSide = null
        )
        assertFalse("Must have specific sustained foot side", day2NoSideEntity.isSustainedHotspot2Day)
    }
}
