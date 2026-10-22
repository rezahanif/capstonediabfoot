package com.project.insole.features.sensor.domain.model

import kotlin.math.abs
import kotlin.math.sqrt

/** Gravity in m/s² — the reference for accel unit detection. */
const val G_TO_MS2 = 9.80665f

/** Radians → degrees — the reference for gyro unit conversion. */
const val RAD_TO_DEG = 57.29577951f

/**
 * Unit normalization handler for firmware sensor payloads.
 *
 * Canonical internal units — the units every FSM threshold and UI label is
 * tuned against — are **accel in g** and **gyro in deg/s**. The firmware's
 * raw CSV values are converted exactly once, HERE, immediately after parsing.
 * If the ESP32 firmware ever changes the units it sends, switching the
 * profile below is the only change required anywhere in the app; the FSM
 * thresholds, storage, and UI never see the difference.
 *
 * The wire format itself (field order and count) is a firmware contract and
 * is NOT touched by this class — only the numeric interpretation.
 */
enum class SensorUnitsProfile(
    val accelToG: Float,
    val gyroToDegPerS: Float
) {
    /**
     * Firmware sends accel in g and gyro in deg/s — the profile the current
     * POC was tuned against (domain SensorConstants: "tuned in G units").
     */
    MPU6050_G_DEGS(1f, 1f),

    /**
     * Firmware sends accel in m/s² and gyro in rad/s (SI output from some
     * MPU-6050 sketches / the README's example line "0.1,-9.8,0.3").
     */
    MPU6050_MS2_RADS(1f / G_TO_MS2, RAD_TO_DEG);

    fun normalizeAccel(value: Float): Float = value * accelToG
    fun normalizeGyro(value: Float): Float = value * gyroToDegPerS
}

/**
 * Auto-detects the firmware's accel scale from the first packets of a
 * session. A stationary IMU measures gravity: |totalAccel| ≈ 1.0 means the
 * firmware sends g; ≈ 9.81 means m/s². The decision uses the median of the
 * first [sampleCount] packets to absorb motion spikes, then locks for the
 * whole session. Gyro has no rest-time reference to auto-detect against, so
 * it follows the same profile (g profile ⇒ deg/s, m/s² profile ⇒ rad/s).
 *
 * Pure JVM logic (no Android imports) so it is unit-testable.
 */
class SensorUnitDetector(
    private val sampleCount: Int = 5,
    private val tolerance: Float = 0.15f
) {
    var profile: SensorUnitsProfile = SensorUnitsProfile.MPU6050_G_DEGS
        private set

    var isDecided: Boolean = false
        private set

    private val magnitudes = ArrayList<Float>(sampleCount)

    /** Clears the decision so a new session re-detects (e.g. after a reflash). */
    fun reset() {
        isDecided = false
        profile = SensorUnitsProfile.MPU6050_G_DEGS
        magnitudes.clear()
    }

    /**
     * Feeds one raw packet and returns the currently active profile —
     * the g profile (MPU6050_G_DEGS) while still sampling, the locked profile
     * once decided.
     */
    fun observe(packet: SensorPacket): SensorUnitsProfile {
        if (isDecided) return profile

        val totalAccel = sqrt(
            packet.accelX * packet.accelX +
                packet.accelY * packet.accelY +
                packet.accelZ * packet.accelZ
        )
        magnitudes.add(totalAccel)

        if (magnitudes.size >= sampleCount) {
            profile = decideFor(median(magnitudes), tolerance)
            isDecided = true
        }
        return profile
    }

    private fun median(values: List<Float>): Float {
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 1) sorted[mid]
        else (sorted[mid - 1] + sorted[mid]) / 2f
    }

    companion object {
        /**
         * Picks the gravity reference the median is closest to, within
         * tolerance. Falls back to the g profile (the POC-tuned default)
         * when neither reference matches, so behaviour degrades to today's
         * rather than inventing a scale.
         */
        fun decideFor(median: Float, tolerance: Float): SensorUnitsProfile {
            val gError = abs(median - 1f)
            val ms2Error = abs(median - G_TO_MS2)
            return when {
                ms2Error <= G_TO_MS2 * tolerance && ms2Error < gError ->
                    SensorUnitsProfile.MPU6050_MS2_RADS
                else -> SensorUnitsProfile.MPU6050_G_DEGS
            }
        }
    }
}
