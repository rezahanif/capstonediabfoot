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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepCounterService @Inject constructor(
    private val stepDao: StepDao,
    private val hourlyStepDao: HourlyStepDao
) {
    private val counter = DualFootStepCounter()
    // DateTimeFormatter is immutable and thread-safe, unlike SimpleDateFormat —
    // this instance is read from multiple concurrent coroutines below.
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var activeDate: String = LocalDate.now().format(dateFormatter)
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    private val _totalStepsFlow = MutableStateFlow(0)
    val totalStepsFlow: StateFlow<Int> = _totalStepsFlow

    private val _hourlyStepsFlow = MutableStateFlow<List<Int>>(List(24) { 0 })
    val hourlyStepsFlow: StateFlow<List<Int>> = _hourlyStepsFlow

    init {
        loadStepsFromDb()
        cleanupOldHistory()
        observeHourlyDb()
        startDayChangeWatchdog()
    }

    private fun startDayChangeWatchdog() {
        serviceScope.launch {
            while (true) {
                delay(1000L)
                val nowStr = LocalDate.now().format(dateFormatter)
                if (nowStr != activeDate) {
                    checkDailyReset(nowStr)
                }
            }
        }
    }

    private fun loadStepsFromDb() {
        serviceScope.launch {
            val todayStr = LocalDate.now().format(dateFormatter)
            activeDate = todayStr
            val entity = stepDao.getStepsForDate(todayStr)
            if (entity != null) {
                // Restore both feet so left/right metrics stay truthful
                // after a restart (the old code dumped everything into LEFT).
                counter.setInitialSteps(entity.count, entity.leftCount, entity.rightCount)
                _totalStepsFlow.value = entity.count
            } else {
                counter.reset()
                _totalStepsFlow.value = 0
            }
        }
    }

    private fun observeHourlyDb() {
        // Cancel the previous eternal Room collector first — without this,
        // every daily reset launched one more collector that lived forever.
        hourlyObserverJob?.cancel()
        hourlyObserverJob = serviceScope.launch {
            val todayStr = LocalDate.now().format(dateFormatter)
            hourlyStepDao.getHourlyStepsForDate(todayStr).collect { entities ->
                val list = MutableList(24) { 0 }
                entities.forEach { entity ->
                    if (entity.hour in 0..23) list[entity.hour] = entity.count
                }
                _hourlyStepsFlow.value = list
            }
        }
    }

    private var hourlyObserverJob: Job? = null

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
            _totalStepsFlow.value = total
            val diff = total - prevTotal
            updateHourlySteps(diff)
            saveStepsToDb(total, counter.leftSteps, counter.rightSteps)
        }
        return total
    }

    /**
     * Forwards a stream-stall to one foot's FSM. Called from
     * BleSensorDataSource's stall watchdog under the packet mutex, so this
     * never runs concurrently with [processPacket].
     */
    fun onStreamStalled(isLeft: Boolean) {
        if (isLeft) counter.leftFSM.onStreamStalled() else counter.rightFSM.onStreamStalled()
    }

    private fun updateHourlySteps(diff: Int) {
        serviceScope.launch {
            // Atomic UPDATE-first/INSERT-fallback — see HourlyStepDao.addSteps.
            // The old SELECT → +diff → REPLACE pattern lost increments when two
            // step events overlapped on Dispatchers.IO.
            hourlyStepDao.addSteps(
                date = LocalDate.now().format(dateFormatter),
                hour = LocalTime.now().hour,
                diff = diff
            )
        }
    }

    private var saveJob: Job? = null
    private fun saveStepsToDb(total: Int, leftSteps: Int, rightSteps: Int) {
        saveJob?.cancel()
        saveJob = serviceScope.launch {
            delay(2000)
            stepDao.insertOrUpdateSteps(
                StepEntity(
                    date = LocalDate.now().format(dateFormatter),
                    count = total,
                    leftCount = leftSteps,
                    rightCount = rightSteps
                )
            )
        }
    }

    fun checkDailyReset(targetDate: String = LocalDate.now().format(dateFormatter)): Boolean {
        if (targetDate != activeDate) {
            android.util.Log.i("StepCounterService", "Day reset triggered: date changed from $activeDate to $targetDate")
            activeDate = targetDate
            resetForDate(targetDate)
            cleanupOldHistory()
            observeHourlyDb()
            return true
        }
        return false
    }

    val totalSteps: Int get() = counter.totalSteps
    val leftSteps: Int get() = counter.leftSteps
    val rightSteps: Int get() = counter.rightSteps
    val walkState get() = counter.dominantState
    fun combinedAccelMag(leftPresent: Boolean, rightPresent: Boolean): Float =
        counter.combinedAccelMag(leftPresent, rightPresent)

    fun resetForDate(dateStr: String = LocalDate.now().format(dateFormatter)) {
        counter.reset()
        _totalStepsFlow.value = 0
        _hourlyStepsFlow.value = List(24) { 0 }
        saveJob?.cancel()
        serviceScope.launch {
            stepDao.insertOrUpdateSteps(
                StepEntity(
                    date = dateStr,
                    count = 0,
                    leftCount = 0,
                    rightCount = 0
                )
            )
        }
    }

    fun reset() {
        resetForDate(LocalDate.now().format(dateFormatter))
    }
}
