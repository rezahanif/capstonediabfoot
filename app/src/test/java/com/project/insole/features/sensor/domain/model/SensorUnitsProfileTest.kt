package com.project.insole.features.sensor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the firmware unit normalization handler.
 *
 * Canonical internal units are g / deg/s. The detector decides the firmware
 * profile from the median |totalAccel| of the first packets at rest
 * (≈ 1.0 ⇒ g, ≈ 9.81 ⇒ m/s²) and locks it for the session.
 */
class SensorUnitsProfileTest {

    // ── conversion factors ───────────────────────────────────────────────────

    @Test
    fun `g profile is identity`() {
        val profile = SensorUnitsProfile.MPU6050_G_DEGS
        assertEquals(1.5f, profile.normalizeAccel(1.5f), 1e-6f)
        assertEquals(-9.8f, profile.normalizeAccel(-9.8f), 1e-6f)
        assertEquals(20f, profile.normalizeGyro(20f), 1e-4f)
    }

    @Test
    fun `ms2 profile converts accel to g`() {
        val profile = SensorUnitsProfile.MPU6050_MS2_RADS
        assertEquals(1f, profile.normalizeAccel(G_TO_MS2), 1e-4f)
        assertEquals(-1f, profile.normalizeAccel(-G_TO_MS2), 1e-4f)
    }

    @Test
    fun `ms2 profile converts rad-s to deg-s`() {
        val profile = SensorUnitsProfile.MPU6050_MS2_RADS
        assertEquals(RAD_TO_DEG, profile.normalizeGyro(1f), 0.001f)
        assertEquals(0f, profile.normalizeGyro(0f), 1e-6f)
    }

    // ── detector ─────────────────────────────────────────────────────────────

    private fun packet(ax: Float = 0f, ay: Float = 0f, az: Float = 0f) =
        SensorPacket(accelX = ax, accelY = ay, accelZ = az)

    @Test
    fun `detects g firmware from rest packets`() {
        val detector = SensorUnitDetector()
        var profile = SensorUnitsProfile.MPU6050_G_DEGS
        repeat(5) { profile = detector.observe(packet(az = -1.02f)) }
        assertTrue(detector.isDecided)
        assertEquals(SensorUnitsProfile.MPU6050_G_DEGS, profile)
    }

    @Test
    fun `detects ms2 firmware from rest packets`() {
        val detector = SensorUnitDetector()
        var profile = SensorUnitsProfile.MPU6050_G_DEGS
        repeat(5) { profile = detector.observe(packet(ay = -9.78f)) }
        assertTrue(detector.isDecided)
        assertEquals(SensorUnitsProfile.MPU6050_MS2_RADS, profile)
    }

    @Test
    fun `motion spike in samples does not break detection - median wins`() {
        val detector = SensorUnitDetector()
        detector.observe(packet(az = -3.0f)) // spike (e.g. user picked the insole up)
        repeat(4) { detector.observe(packet(az = -1.0f)) }
        assertTrue(detector.isDecided)
        assertEquals(SensorUnitsProfile.MPU6050_G_DEGS, detector.profile)
    }

    @Test
    fun `undecided detector still returns default g profile`() {
        val detector = SensorUnitDetector()
        assertEquals(SensorUnitsProfile.MPU6050_G_DEGS, detector.observe(packet(ay = -9.8f)))
        assertFalse(detector.isDecided)
    }

    @Test
    fun `unresolvable median falls back to g profile`() {
        // |totalAccel| ≈ 5 matches neither 1.0 nor 9.81 within ±15 %
        assertEquals(SensorUnitsProfile.MPU6050_G_DEGS, SensorUnitDetector.decideFor(5f, 0.15f))
    }

    @Test
    fun `locked detector ignores further samples`() {
        val detector = SensorUnitDetector()
        repeat(5) { detector.observe(packet(ay = -9.8f)) }
        assertTrue(detector.isDecided)
        repeat(10) { detector.observe(packet(az = -1.0f)) } // would flip the decision if re-sampling
        assertEquals(SensorUnitsProfile.MPU6050_MS2_RADS, detector.profile)
    }

    @Test
    fun `reset reopens detection`() {
        val detector = SensorUnitDetector()
        repeat(5) { detector.observe(packet(ay = -9.8f)) }
        assertTrue(detector.isDecided)

        detector.reset()

        assertFalse(detector.isDecided)
        assertEquals(SensorUnitsProfile.MPU6050_G_DEGS, detector.profile)
        assertEquals(SensorUnitsProfile.MPU6050_G_DEGS, detector.observe(packet(az = -1.0f)))
        assertFalse(detector.isDecided)
    }
}
