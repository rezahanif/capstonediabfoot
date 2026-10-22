package com.project.insole.core.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReconnectPolicyTest {

    @Test
    fun `allows retries up to the configured maximum`() {
        val policy = ReconnectPolicy(maxAttempts = 5, retryIntervalMs = 5000L)

        repeat(5) {
            assertTrue("attempt ${policy.attempts + 1} should be allowed", policy.shouldRetry())
            policy.onRetryScheduled()
        }

        assertFalse(policy.shouldRetry())
        assertEquals(5, policy.attempts)
    }

    @Test
    fun `uses the planned go-to parameters by default`() {
        val policy = ReconnectPolicy()
        assertEquals(5, ReconnectPolicy.MAX_RECONNECTION_ATTEMPTS)
        assertEquals(5000L, ReconnectPolicy.RECONNECTION_INTERVAL_MS)
        assertEquals(5000L, policy.retryIntervalMs)
    }

    @Test
    fun `successful connection restores the retry budget`() {
        val policy = ReconnectPolicy(maxAttempts = 5)
        repeat(5) { policy.onRetryScheduled() }
        assertFalse(policy.shouldRetry())

        policy.onConnected()

        assertTrue(policy.shouldRetry())
        assertEquals(0, policy.attempts)
    }

    @Test
    fun `reset clears the budget without a connection`() {
        val policy = ReconnectPolicy(maxAttempts = 5)
        repeat(3) { policy.onRetryScheduled() }

        policy.reset()

        assertTrue(policy.shouldRetry())
        assertEquals(0, policy.attempts)
    }
}
