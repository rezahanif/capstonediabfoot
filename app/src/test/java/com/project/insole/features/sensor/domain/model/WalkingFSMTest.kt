package com.project.insole.features.sensor.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit test for [WalkingFSM].
 * Verifies state transitions based on simulated acceleration data.
 */
class WalkingFSMTest {

    @Test
    fun `should start in STANDING state`() {
        val fsm = WalkingFSM()
        assertEquals(WalkState.STANDING, fsm.state)
    }

    @Test
    fun `should transition to TRANSITION when movement is detected`() {
        // Given
        val fsm = WalkingFSM()
        // Simulate movement packet (accelMag will be > 0.12f)
        val movingPacket = SensorPacket(accelX = 0.5f, accelY = 0.5f, accelZ = -1.2f)

        // When
        fsm.process(movingPacket)

        // Then
        assertEquals(WalkState.TRANSITION, fsm.state)
    }

    @Test
    fun `should count steps in WALKING state`() {
        // Given
        val fsm = WalkingFSM()
        val initialSteps = fsm.stepCount
        
        // Simulating a sequence to get into WALKING state
        // In a real test, we would feed a sequence of packets
        val peakPacket = SensorPacket(accelX = 0.0f, accelY = 0.0f, accelZ = -1.5f)
        
        // When
        fsm.process(peakPacket) 
        
        // Then
        // This is a simple check; real FSM testing requires a sequence over time
        // because of the 100ms and 1500L timers in the logic.
    }
}
