package com.project.insole.core.database.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyFootMetricTest {

    @Test
    fun `zero samples yields zero average and zero asymmetry`() {
        val entity = DailyFootMetricEntity(date = "2026-10-25")
        assertEquals(0f, entity.leftAvgTemp, 1e-4f)
        assertEquals(0f, entity.rightAvgTemp, 1e-4f)
        assertEquals(0f, entity.thermalAsymmetryC, 1e-4f)
    }

    @Test
    fun `one foot sampled does not calculate false asymmetry`() {
        val entity = DailyFootMetricEntity(
            date = "2026-10-25",
            leftMinTemp = 30.0f,
            leftMaxTemp = 32.0f,
            leftSumTemp = 62.0,
            leftSamples = 2,
            rightSamples = 0
        )
        assertEquals(31.0f, entity.leftAvgTemp, 1e-4f)
        assertEquals(0f, entity.rightAvgTemp, 1e-4f)
        assertEquals(0f, entity.thermalAsymmetryC, 1e-4f)
    }

    @Test
    fun `bilateral samples computes accurate average and absolute asymmetry`() {
        val entity = DailyFootMetricEntity(
            date = "2026-10-25",
            leftMinTemp = 30.0f,
            leftMaxTemp = 34.0f,
            leftSumTemp = 320.0,
            leftSamples = 10,  // avg = 32.0°C
            rightMinTemp = 29.0f,
            rightMaxTemp = 31.0f,
            rightSumTemp = 295.0,
            rightSamples = 10  // avg = 29.5°C
        )
        assertEquals(32.0f, entity.leftAvgTemp, 1e-4f)
        assertEquals(29.5f, entity.rightAvgTemp, 1e-4f)
        assertEquals(2.5f, entity.thermalAsymmetryC, 1e-4f)
    }
}
