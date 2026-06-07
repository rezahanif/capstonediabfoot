package com.project.insole.features.sensor.domain.service

import com.project.insole.core.database.dao.StepDao
import com.project.insole.core.database.entity.StepEntity
import com.project.insole.features.sensor.domain.model.DualFootStepCounter
import com.project.insole.features.sensor.domain.model.SensorPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain-level service that maintains the state of the step counter.
 * Includes auto-reset logic for new days and local database persistence.
 */
@Singleton
class StepCounterService @Inject constructor(
    private val stepDao: StepDao
) {
    private val counter = DualFootStepCounter()
    private var lastResetDayOfYear: Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadStepsFromDb()
        cleanupOldHistory()
    }

    private fun loadStepsFromDb() {
        serviceScope.launch {
            val todayStr = dateFormat.format(Date())
            val entity = stepDao.getStepsForDate(todayStr)
            if (entity != null) {
                android.util.Log.d("StepCounterService", "Loaded ${entity.count} steps from DB for $todayStr")
                counter.setInitialSteps(entity.count)
            }
        }
    }

    private fun cleanupOldHistory() {
        serviceScope.launch {
            stepDao.deleteOldSteps(62)
            android.util.Log.d("StepCounterService", "Cleaned up database: Keeping only newest 62 days.")
        }
    }

    fun processPacket(packet: SensorPacket, isLeft: Boolean): Int {
        checkDailyReset()
        val total = if (isLeft) {
            counter.processLeft(packet)
        } else {
            counter.processRight(packet)
        }
        saveStepsToDb(total)
        return total
    }

    private var saveJob: Job? = null
    private fun saveStepsToDb(total: Int) {
        // Debounce saves to avoid constant DB writes
        saveJob?.cancel()
        saveJob = serviceScope.launch {
            delay(2000) // Wait 2 seconds of inactivity before saving
            val todayStr = dateFormat.format(Date())
            stepDao.insertOrUpdateSteps(StepEntity(todayStr, total))
            android.util.Log.d("StepCounterService", "Saved $total steps to DB")
        }
    }

    private fun checkDailyReset() {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        if (currentDay != lastResetDayOfYear) {
            android.util.Log.d("StepCounterService", "New day detected, resetting steps.")
            reset()
            cleanupOldHistory()
            lastResetDayOfYear = currentDay
        }
    }

    val totalSteps: Int get() = counter.totalSteps
    val walkState get() = counter.dominantState
    val combinedAccelMag get() = counter.combinedAccelMag

    fun reset() {
        counter.reset()
        // Also clear in DB for today
        serviceScope.launch {
            val todayStr = dateFormat.format(Date())
            stepDao.insertOrUpdateSteps(StepEntity(todayStr, 0))
        }
    }
}
