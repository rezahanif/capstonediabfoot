package com.project.insole.core.ble

/**
 * Pure retry bookkeeping for automatic reconnection — one instance per
 * device side. No Android imports, fully unit-testable; the caller owns
 * the actual delay/scheduling.
 */
class ReconnectPolicy(
    private val maxAttempts: Int = MAX_RECONNECTION_ATTEMPTS,
    val retryIntervalMs: Long = RECONNECTION_INTERVAL_MS
) {
    var attempts: Int = 0
        private set

    /** True while the retry budget is not exhausted. */
    fun shouldRetry(): Boolean = attempts < maxAttempts

    /** Call right before scheduling a retry attempt. */
    fun onRetryScheduled() {
        attempts++
    }

    /** Call on a successful connection — a stable link restores the budget. */
    fun onConnected() {
        attempts = 0
    }

    /** Call when reconnecting becomes irrelevant (device unpaired, etc.). */
    fun reset() {
        attempts = 0
    }

    companion object {
        const val MAX_RECONNECTION_ATTEMPTS = 5
        const val RECONNECTION_INTERVAL_MS = 5000L
    }
}
