package com.project.insole.features.sensor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Parser tests for the firmware CSV line "ax,ay,az,gx,gy,gz,pressure,temp".
 *
 * The wire format is a firmware contract — these tests only pin the
 * interpretation: exactly 8 float fields, strict rejection of corrupt
 * values (a substituted 0f would inject fake "at rest" samples into the
 * FSM and fake values into the medical peak tracking).
 */
class SensorPacketTest {

    private val validLine = "0.1,-9.8,0.3,0.0,0.0,0.1,127.0,34.2"

    @Test
    fun `parses 8 csv fields`() {
        val p = SensorPacket.fromBleString(validLine)!!

        assertEquals(0.1f, p.accelX, 1e-6f)
        assertEquals(-9.8f, p.accelY, 1e-6f)
        assertEquals(0.3f, p.accelZ, 1e-6f)
        assertEquals(0.0f, p.gyroX, 1e-6f)
        assertEquals(0.0f, p.gyroY, 1e-6f)
        assertEquals(0.1f, p.gyroZ, 1e-6f)
        assertEquals(127.0f, p.pressure, 1e-6f)
        assertEquals(34.2f, p.temperature, 1e-6f)
    }

    @Test
    fun `tolerates surrounding whitespace`() {
        val p = SensorPacket.fromBleString(" 0.1 , -9.8 , 0.3 , 0.0 , 0.0 , 0.1 , 127.0 , 34.2 ")!!
        assertEquals(-9.8f, p.accelY, 1e-6f)
        assertEquals(34.2f, p.temperature, 1e-6f)
    }

    @Test
    fun `short packet is rejected`() {
        assertNull(SensorPacket.fromBleString("0.1,-9.8,0.3"))
        assertNull(SensorPacket.fromBleString(""))
    }

    @Test
    fun `corrupt field rejects the whole packet`() {
        // Old behaviour coerced 'abc' to 0f — a fake "at rest" sample.
        assertNull(SensorPacket.fromBleString("0.1,abc,0.3,0.0,0.0,0.1,127.0,34.2"))
    }

    @Test
    fun `nan and infinity fields reject the whole packet`() {
        // Both parse as floats and would poison the EMA filter forever.
        assertNull(SensorPacket.fromBleString("0.1,NaN,0.3,0.0,0.0,0.1,127.0,34.2"))
        assertNull(SensorPacket.fromBleString("0.1,Infinity,0.3,0.0,0.0,0.1,127.0,34.2"))
    }

    @Test
    fun `extra trailing fields are ignored`() {
        val p = SensorPacket.fromBleString("$validLine,99.9")!!
        assertEquals(34.2f, p.temperature, 1e-6f)
    }
}
