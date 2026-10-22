package com.project.insole.features.sensor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Parser tests for the firmware's three CSV telemetry line types:
 *  - v2 stream:      "ax,ay,az,gx,gy,gz,p1,p2,p3,p4,p5"  (11 numeric fields)
 *  - legacy v1:      "ax,ay,az,gx,gy,gz,pressure,tempC"  (8 numeric fields)
 *  - v2 heartbeat:   "T,tempC[,batteryPercent]"
 *
 * The wire format is a firmware contract — these tests pin the
 * interpretation and the strict rejection rules (a substituted 0f would
 * inject fake "at rest" samples into the FSM and fake values into the
 * medical peak tracking).
 */
class SensorPacketParserTest {

    // ax,ay,az,gx,gy,gz = 0.1,-9.8,0.3,0.0,0.5,-0.1 ; p1..p5 (kPa) below.
    private val v2Line = "0.1,-9.8,0.3,0.0,0.5,-0.1,12.5,20.0,15.0,8.0,40.0"

    @Test
    fun `parses v2 stream with values in FSR_ZONE_ORDER and pressure as sum`() {
        val p = SensorPacketParser.parse(v2Line)!!

        assertEquals(0.1f, p.accelX, 1e-6f)
        assertEquals(-9.8f, p.accelY, 1e-6f)
        assertEquals(0.3f, p.accelZ, 1e-6f)
        assertEquals(0.0f, p.gyroX, 1e-6f)
        assertEquals(0.5f, p.gyroY, 1e-6f)
        assertEquals(-0.1f, p.gyroZ, 1e-6f)

        // p1..p5 map onto FSR_ZONE_ORDER: Hallux, MTH1, MTH2, MTH4, Heel.
        assertEquals(FSR_ZONE_ORDER, listOf(
            PlantarZone.HALLUX, PlantarZone.MTH1, PlantarZone.MTH2,
            PlantarZone.MTH4, PlantarZone.HEEL,
        ))
        assertEquals(5, p.pressuresKpa.size)
        assertEquals(12.5f, p.pressuresKpa[0], 1e-6f)   // Hallux
        assertEquals(20.0f, p.pressuresKpa[1], 1e-6f)   // MTH1
        assertEquals(15.0f, p.pressuresKpa[2], 1e-6f)   // MTH2
        assertEquals(8.0f, p.pressuresKpa[3], 1e-6f)    // MTH4
        assertEquals(40.0f, p.pressuresKpa[4], 1e-6f)   // Heel

        assertEquals(12.5f + 20.0f + 15.0f + 8.0f + 40.0f, p.pressure, 1e-4f)
        assertEquals(0f, p.temperature, 1e-6f)          // v2 temp rides the "T," line only
        assertTrue(p.imuValid)
        assertFalse(p.isTemperatureOnly)
        assertNull(p.batteryPercent)
    }

    @Test
    fun `two-field temperature line is a valid temperature-only packet`() {
        val p = SensorPacketParser.parse("T,32.4")!!

        assertEquals(32.4f, p.temperature, 1e-6f)
        assertTrue(p.isTemperatureOnly)
        assertFalse(p.imuValid)
        assertTrue(p.pressuresKpa.isEmpty())
        assertEquals(0f, p.pressure, 1e-6f)
        assertNull(p.batteryPercent)
    }

    @Test
    fun `three-field temperature line carries battery percent`() {
        val p = SensorPacketParser.parse("T,32.4,87")!!

        assertEquals(32.4f, p.temperature, 1e-6f)
        assertEquals(87, p.batteryPercent)
        assertTrue(p.isTemperatureOnly)
    }

    @Test
    fun `ntc failure sentinel -999 is rejected`() {
        // Firmware reports -999 when the NTC read fails.
        assertNull(SensorPacketParser.parse("T,-999"))
        assertNull(SensorPacketParser.parse("T,-999,80"))
    }

    @Test
    fun `temperature above 55 c is rejected`() {
        assertNull(SensorPacketParser.parse("T,60"))
        assertNull(SensorPacketParser.parse("T,60,50"))
    }

    @Test
    fun `battery percent outside 0-100 is rejected`() {
        assertNull(SensorPacketParser.parse("T,32.4,101"))
        assertNull(SensorPacketParser.parse("T,32.4,-1"))
    }

    @Test
    fun `boundary temperatures and battery are accepted`() {
        assertEquals(-5f, SensorPacketParser.parse("T,-5")!!.temperature, 1e-6f)
        assertEquals(55f, SensorPacketParser.parse("T,55")!!.temperature, 1e-6f)
        assertEquals(0, SensorPacketParser.parse("T,30,0")!!.batteryPercent)
        assertEquals(100, SensorPacketParser.parse("T,30,100")!!.batteryPercent)
    }

    @Test
    fun `legacy 8-field stream parses via parser and fromBleString delegate`() {
        val line = "0.1,-9.8,0.3,0.0,0.0,0.1,127.0,34.2"

        val viaParser = SensorPacketParser.parse(line)!!
        assertEquals(127.0f, viaParser.pressure, 1e-6f)
        assertEquals(34.2f, viaParser.temperature, 1e-6f)
        assertEquals(listOf(127.0f), viaParser.pressuresKpa)
        assertTrue(viaParser.imuValid)
        assertFalse(viaParser.isTemperatureOnly)

        val viaDelegate = SensorPacket.fromBleString(line)!!
        assertEquals(viaParser, viaDelegate)
    }

    @Test
    fun `all-zero IMU marks imuValid false in v2 stream`() {
        val p = SensorPacketParser.parse("0,0,0,0,0,0,1.0,2.0,3.0,4.0,5.0")!!
        assertFalse(p.imuValid)
        // Pressures must still be captured — only the IMU is dead.
        assertEquals(1.0f, p.pressuresKpa[0], 1e-6f)
        assertEquals(15.0f, p.pressure, 1e-4f)
    }

    @Test
    fun `all-zero IMU marks imuValid false in legacy stream`() {
        val p = SensorPacketParser.parse("0,0,0,0,0,0,10.0,30.0")!!
        assertFalse(p.imuValid)
        assertEquals(10.0f, p.pressure, 1e-6f)
    }

    @Test
    fun `near-zero-but-not-exact IMU keeps imuValid true`() {
        // A resting foot reports ≈ ±1 g on some axis; 0.001 g is a real sensor.
        val p = SensorPacketParser.parse("0.001,0,0,0,0,0,1.0,2.0,3.0,4.0,5.0")!!
        assertTrue(p.imuValid)
    }

    @Test
    fun `negative pressure is rejected`() {
        assertNull(SensorPacketParser.parse("0.1,-9.8,0.3,0.0,0.5,-0.1,-1.0,20.0,15.0,8.0,40.0"))
        assertNull(SensorPacketParser.parse("0.1,-9.8,0.3,0.0,0.0,0.1,-5.0,34.2"))
    }

    @Test
    fun `nan and infinity fields are rejected`() {
        assertNull(SensorPacketParser.parse("0.1,NaN,0.3,0.0,0.5,-0.1,1.0,2.0,3.0,4.0,5.0"))
        assertNull(SensorPacketParser.parse("0.1,-9.8,0.3,0.0,0.5,Infinity,1.0,2.0,3.0,4.0,5.0"))
        assertNull(SensorPacketParser.parse("0.1,-9.8,0.3,0.0,0.5,-0.1,NaN,2.0,3.0,4.0,5.0"))
        assertNull(SensorPacketParser.parse("0.1,-9.8,0.3,0.0,0.0,0.1,127.0,Infinity"))
        assertNull(SensorPacketParser.parse("T,NaN"))
    }

    @Test
    fun `non-numeric field rejects the whole packet`() {
        assertNull(SensorPacketParser.parse("0.1,abc,0.3,0.0,0.5,-0.1,1.0,2.0,3.0,4.0,5.0"))
        assertNull(SensorPacketParser.parse("0.1,-9.8,0.3,0.0,0.0,0.1,127.0,abc"))
    }

    @Test
    fun `whitespace is tolerated`() {
        val p = SensorPacketParser.parse(" 0.1 , -9.8 , 0.3 , 0.0 , 0.5 , -0.1 , 12.5 , 20.0 , 15.0 , 8.0 , 40.0 ")!!
        assertEquals(-9.8f, p.accelY, 1e-6f)
        assertEquals(40.0f, p.pressuresKpa[4], 1e-6f)

        val t = SensorPacketParser.parse(" T , 32.4 , 87 ")!!
        assertEquals(32.4f, t.temperature, 1e-6f)
        assertEquals(87, t.batteryPercent)
    }

    @Test
    fun `extra trailing fields are ignored`() {
        val p = SensorPacketParser.parse("$v2Line,99.9,extra")!!
        assertEquals(40.0f, p.pressuresKpa[4], 1e-6f)

        val legacy = SensorPacketParser.parse("0.1,-9.8,0.3,0.0,0.0,0.1,127.0,34.2,99.9")!!
        assertEquals(34.2f, legacy.temperature, 1e-6f)
    }

    @Test
    fun `too-short lines are rejected`() {
        assertNull(SensorPacketParser.parse(""))
        assertNull(SensorPacketParser.parse("0.1,-9.8,0.3"))
        assertNull(SensorPacketParser.parse("0.1,-9.8,0.3,0.0,0.5,-0.1,1.0"))
        assertNull(SensorPacketParser.parse("T"))
    }

    @Test
    fun `mixed legacy and v2 lines parse independently`() {
        val legacy = SensorPacketParser.parse("0.1,-9.8,0.3,0.0,0.0,0.1,127.0,34.2")!!
        val v2 = SensorPacketParser.parse(v2Line)!!
        val heartbeat = SensorPacketParser.parse("T,31.8,64")!!

        // Legacy keeps its inline temperature and single-zone payload.
        assertEquals(34.2f, legacy.temperature, 1e-6f)
        assertEquals(1, legacy.pressuresKpa.size)
        // v2 has five zones and no inline temperature.
        assertEquals(5, v2.pressuresKpa.size)
        assertEquals(0f, v2.temperature, 1e-6f)
        // Heartbeat has temperature + battery and no pressure payload.
        assertEquals(31.8f, heartbeat.temperature, 1e-6f)
        assertEquals(64, heartbeat.batteryPercent)
        assertTrue(heartbeat.pressuresKpa.isEmpty())
    }
}
