package com.project.insole.features.sensor.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WearGatingTest {

    @Test
    fun physiologicalTemperatureRange_rejectsAmbientAndSentinel() {
        // Room ambient off-shoe temperatures
        assertFalse(WearGating.isPhysiologicalSkinTemp(20.0f))
        assertFalse(WearGating.isPhysiologicalSkinTemp(24.5f))
        assertFalse(WearGating.isPhysiologicalSkinTemp(26.9f))

        // Firmware error sentinels
        assertFalse(WearGating.isPhysiologicalSkinTemp(-999.0f))
        assertFalse(WearGating.isPhysiologicalSkinTemp(0.0f))

        // Non-physiological extreme heat
        assertFalse(WearGating.isPhysiologicalSkinTemp(40.0f))

        // Plausible skin temperature (Armstrong/Lavery)
        assertTrue(WearGating.isPhysiologicalSkinTemp(27.0f))
        assertTrue(WearGating.isPhysiologicalSkinTemp(31.5f))
        assertTrue(WearGating.isPhysiologicalSkinTemp(34.2f))
        assertTrue(WearGating.isPhysiologicalSkinTemp(38.5f))
    }

    @Test
    fun isCleanOnBody_rejectsOffBodyRestingAndPinNoise() {
        // 1. Off-body resting on desk (room temp 22°C, 0 pressure)
        val restingPacket = SensorPacket(
            pressure = 0f,
            pressuresKpa = listOf(0f, 0f, 0f, 0f, 0f),
            temperature = 22.0f,
            accelX = 0f, accelY = 0f, accelZ = 1f,
            gyroX = 0f, gyroY = 0f, gyroZ = 0f
        )
        assertFalse(WearGating.isCleanOnBody(restingPacket))

        // 2. Firmware bug: 1 pin sending noise (15 kPa) but ambient temperature 23°C
        val pinGlitchPacket = SensorPacket(
            pressure = 15f,
            pressuresKpa = listOf(15f, 0f, 0f, 0f, 0f),
            temperature = 23.0f,
            accelX = 0f, accelY = 0f, accelZ = 1f,
            gyroX = 0f, gyroY = 0f, gyroZ = 0f
        )
        assertFalse("Single pin noise with ambient temperature must not be treated as clean wear",
            WearGating.isCleanOnBody(pinGlitchPacket))

        // 3. Genuine wear: 3+ sensors loaded and skin temp 32°C
        val genuineWearPacket = SensorPacket(
            pressure = 120f,
            pressuresKpa = listOf(30f, 25f, 40f, 15f, 10f),
            temperature = 32.0f,
            accelX = 0f, accelY = 0f, accelZ = 1f,
            gyroX = 0f, gyroY = 0f, gyroZ = 0f
        )
        assertTrue(WearGating.isCleanOnBody(genuineWearPacket))
    }

    @Test
    fun isActiveStance_identifiesWeightBearingOnly() {
        val swingPacket = SensorPacket(
            pressure = 0f,
            pressuresKpa = listOf(0f, 0f, 0f, 0f, 0f),
            temperature = 32.0f,
            accelX = 0f, accelY = 0f, accelZ = 1f,
            gyroX = 0f, gyroY = 0f, gyroZ = 0f
        )
        assertFalse("Swing / unweighted phase is not active stance",
            WearGating.isActiveStance(swingPacket, WalkState.TRANSITION))

        val stancePacket = SensorPacket(
            pressure = 150f,
            pressuresKpa = listOf(40f, 50f, 60f, 0f, 0f),
            temperature = 32.0f,
            accelX = 0f, accelY = 0f, accelZ = 1f,
            gyroX = 0f, gyroY = 0f, gyroZ = 0f
        )
        assertTrue("Weight-bearing >= 20 kPa is active stance",
            WearGating.isActiveStance(stancePacket, WalkState.WALKING))
    }
}
