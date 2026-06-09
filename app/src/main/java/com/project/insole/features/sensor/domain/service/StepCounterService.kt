package com.project.insole.features.sensor.domain.service

import com.project.insole.core.database.dao.HourlyStepDao
import com.project.insole.core.database.dao.StepDao
import com.project.insole.core.database.entity.HourlyStepEntity
import com.project.insole.core.database.entity.StepEntity
import com.project.insole.features.sensor.domain.model.DualFootStepCounter
import com.project.insole.features.sensor.domain.model.SensorPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepCounterService @Inject constructor(
    private val stepDao: StepDao,
    private val hourlyStepDao: HourlyStepDao
) {
    private val counter = DualFootStepCounter()
    private var lastResetDayOfYear: Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _hourlyStepsFlow = MutableStateFlow<List<Int>>(List(24) { 0 })
    val hourlyStepsFlow: StateFlow<List<Int>> = _hourlyStepsFlow

    init {
        loadStepsFromDb()
        cleanupOldHistory()
        observeHourlyDb()
    }

    private fun loadStepsFromDb() {
        serviceScope.launch {
            val todayStr = dateFormat.format(Date())
            val entity = stepDao.getStepsForDate(todayStr)
            if (entity != null) {
                counter.setInitialSteps(entity.count)
            }
        }
    }

    private fun observeHourlyDb() {
        serviceScope.launch {
            val todayStr = dateFormat.format(Date())
            hourlyStepDao.getHourlyStepsForDate(todayStr).collect { entities ->
                val list = MutableList(24) { 0 }
                entities.forEach { entity ->
                    if (entity.hour in 0..23) list[entity.hour] = entity.count
                }
                _hourlyStepsFlow.value = list
            }
        }
    }

    private fun cleanupOldHistory() {
        serviceScope.launch {
            stepDao.deleteOldSteps(62)
            hourlyStepDao.deleteOldHourlySteps(8)
            android.util.Log.d("StepCounterService", "Cleaned up database: 62 days daily, 8 days hourly.")
        }
    }

    fun processPacket(packet: SensorPacket, isLeft: Boolean): Int {
        checkDailyReset()
        val prevTotal = counter.totalSteps
        val total = if (isLeft) {
            counter.processLeft(packet)
        } else {
            counter.processRight(packet)
        }
        
        if (total > prevTotal) {
            val diff = total - prevTotal
            updateHourlySteps(diff)
            saveStepsToDb(total)
        }
        return total
    }

    private fun updateHourlySteps(diff: Int) {
        serviceScope.launch {
            val todayStr = dateFormat.format(Date())
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val existing = hourlyStepDao.getStepsForHour(todayStr, currentHour)
            val newCount = (existing?.count ?: 0) + diff
            hourlyStepDao.insertOrUpdate(HourlyStepEntity(todayStr, currentHour, newCount))
        }
    }

    private var saveJob: Job? = null
    private fun saveStepsToDb(total: Int) {
        saveJob?.cancel()
        saveJob = serviceScope.launch {
            delay(2000)
            val todayStr = dateFormat.format(Date())
            stepDao.insertOrUpdateSteps(StepEntity(todayStr, total))
        }
    }

    private fun checkDailyReset() {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        if (currentDay != lastResetDayOfYear) {
            reset()
            cleanupOldHistory()
            lastResetDayOfYear = currentDay
            observeHourlyDb() // Refresh observer for new day
        }
    }

    val totalSteps: Int get() = counter.totalSteps
    val leftSteps: Int get() = counter.leftSteps
    val rightSteps: Int get() = counter.rightSteps
    val walkState get() = counter.dominantState
    val combinedAccelMag get() = counter.combinedAccelMag

    fun reset() {
        counter.reset()
        serviceScope.launch {
            val todayStr = dateFormat.format(Date())
            stepDao.insertOrUpdateSteps(StepEntity(todayStr, 0))
        }
    }
}
