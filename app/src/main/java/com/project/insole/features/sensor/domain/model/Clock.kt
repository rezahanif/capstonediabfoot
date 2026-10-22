package com.project.insole.features.sensor.domain.model

/**
 * Abstraction over system time.
 * Production code uses [SystemClock]; tests inject [FakeClock].
 *
 * This is the "humble object" pattern — isolate the untestable
 * dependency (wall-clock time) behind an interface so the FSM
 * logic itself becomes fully deterministic in tests.
 */
interface Clock {
    fun currentTimeMillis(): Long
}

/** Real implementation: monotonic time since boot.
 *
 *  elapsedRealtime() (not wall-clock currentTimeMillis()) is used because the
 *  FSM measures intervals — a wall-clock jump (NTP sync, manual time change)
 *  would distort its 100 ms / 1500 ms gates. The method name keeps the
 *  interface contract; the domain is "milliseconds on a monotonic clock". */
object SystemClock : Clock {
    override fun currentTimeMillis(): Long = android.os.SystemClock.elapsedRealtime()
}

/**
 * Fake clock for tests.
 * Time is frozen at [currentTime] until you call [advanceBy].
 *
 * Usage:
 *   val clock = FakeClock(startTime = 1000L)
 *   clock.advanceBy(200L)  // now at 1200L
 */
class FakeClock(private var startTime: Long = 0L) : Clock {
    private var currentTime: Long = startTime
    override fun currentTimeMillis(): Long = currentTime
    fun advanceBy(ms: Long) { currentTime += ms }
}
