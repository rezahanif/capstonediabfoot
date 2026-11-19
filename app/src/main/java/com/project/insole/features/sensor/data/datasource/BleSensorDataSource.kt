package com.project.insole.features.sensor.data.datasource

import android.annotation.SuppressLint
import com.project.insole.core.ble.InsoleBleManager
import com.project.insole.core.database.dao.DailyFootMetricDao
import com.project.insole.core.database.entity.DailyFootMetricEntity
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.model.SensorPacket
import com.project.insole.features.sensor.domain.model.SensorPacketParser
import com.project.insole.features.sensor.domain.model.SensorUnitDetector
import com.project.insole.features.sensor.domain.model.SensorUnitsProfile
import com.project.insole.features.sensor.domain.service.StepCounterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import com.project.insole.features.notifications.domain.FootSide
import com.project.insole.features.sensor.domain.model.ClinicalScoringVariables
import com.project.insole.features.sensor.domain.model.SensorConstants
import com.project.insole.features.sensor.domain.model.WearGating

import com.project.insole.features.diagnostics.presentation.seeder.YesterdayMetricsReloader

/** Silence longer than this (≈10 lost packets at 5 Hz) means one foot's FSM is stale. */
private const val STALE_PACKET_TIMEOUT_MS = 2000L
private const val STALL_WATCHDOG_INTERVAL_MS = 1000L

/**
 * Singleton source of truth for parsed BLE telemetry.
 * Subscribes to raw telemetryFlow, parses into SensorPackets,
 * aggregates them into domain-friendly InsoleSensorData, and
 * accumulates daily clinical metrics (thermal & pressure) for Room persistence (TD-7).
 */
@SuppressLint("MissingPermission")
@Singleton
class BleSensorDataSource @Inject constructor(
    private val bleManager: InsoleBleManager,
    private val stepCounterService: StepCounterService,
    private val processStepCountUseCase: com.project.insole.features.sensor.domain.usecase.ProcessStepCountUseCase,
    private val dailyFootMetricDao: DailyFootMetricDao
) : YesterdayMetricsReloader {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sensorDataFlow = MutableStateFlow<InsoleSensorData?>(null)
    val sensorDataFlow: Flow<InsoleSensorData?> = _sensorDataFlow

    private val _rawLeftDataFlow = MutableStateFlow<String?>(null)
    val rawLeftDataFlow: Flow<String?> = _rawLeftDataFlow

    private val _rawRightDataFlow = MutableStateFlow<String?>(null)
    val rawRightDataFlow: Flow<String?> = _rawRightDataFlow

    private val _hourlyStepsFlow = MutableStateFlow<List<Int>>(List(24) { 0 })
    val hourlyStepsFlow: Flow<List<Int>> = _hourlyStepsFlow

    private var currentLeftPacket: SensorPacket? = null
    private var currentRightPacket: SensorPacket? = null

    // Latest NTC readings from the v2 "T,..." temperature-only heartbeat
    // (1/min). Kept separate from the stream packets: a v2 stream packet
    // carries no temperature, and a heartbeat must never be mistaken for a
    // live 5 Hz stream. Guarded by packetMutex.
    private var latestLeftTempC = 0f
    private var latestRightTempC = 0f

    // Battery percent from the v2 heartbeat, when the firmware reports it.
    // Null = firmware has no battery monitoring (TD-13). Guarded by packetMutex.
    private var latestBatteryPercent: Int? = null

    private var leftPeakTemp = 0f
    private var rightPeakTemp = 0f

    private var leftPeakPressure = 0
    private var rightPeakPressure = 0

    // Serializes packet processing (IO collector) against resetSessionStats
    // (invoked from the main thread) — the peak/packet fields are plain vars
    // and must not be mutated concurrently.
    private val packetMutex = Mutex()

    // L1 unit handler: locks the firmware's unit profile once per session so
    // a firmware unit change never reaches the FSM or the UI.
    private val unitDetector = SensorUnitDetector()
    private var unitProfileLogged = false

    // L4 stream-stall watchdog state: elapsedRealtime of the last packet per
    // foot; 0 = no packet seen / stall already handled. Guarded by packetMutex.
    private var lastLeftPacketMs = 0L
    private var lastRightPacketMs = 0L

    // TD-7: Local Daily Clinical Foot Metric Accumulator (Room persistence).
    // Guarded by packetMutex.
    private var activeDate = LocalDate.now().toString()
    private var dailyLeftMinTemp = 0f
    private var dailyLeftMaxTemp = 0f
    private var dailyLeftSumTemp = 0.0
    private var dailyLeftSamples = 0
    private var dailyRightMinTemp = 0f
    private var dailyRightMaxTemp = 0f
    private var dailyRightSumTemp = 0.0
    private var dailyRightSamples = 0
    private var dailyLeftPeakPressureKpa = 0f
    private var dailyRightPeakPressureKpa = 0f
    private var dailyWearSeconds = 0L
    private var watchdogTicksSinceLastFlush = 0

    // Clean data accumulators (wear-gated, noise-filtered)
    private var dailyCleanLeftMinTemp = 0f
    private var dailyCleanLeftMaxTemp = 0f
    private var dailyCleanLeftSumTemp = 0.0
    private var dailyCleanLeftSamples = 0
    private var dailyCleanRightMinTemp = 0f
    private var dailyCleanRightMaxTemp = 0f
    private var dailyCleanRightSumTemp = 0.0
    private var dailyCleanRightSamples = 0
    private var dailyCleanWearSeconds = 0L
    private var dailyCleanStancePressureSumKpa = 0.0
    private var dailyCleanStanceSamples = 0

    // Hourly stance pressure tracking
    private var currentHour = -1
    private var currentHourStancePressureSum = 0.0
    private var currentHourStanceSamples = 0

    // 2-Day consecutive hotspot tracking (Armstrong et al., IWGDF)
    private var consecutiveThermalDays = 0
    private var sustainedThermalSide: String? = null
    private var yesterdayThermalAsymmetryC = 0f
    private var yesterdayWarmerFoot: FootSide? = null

    private val _clinicalScoringFlow = MutableStateFlow(ClinicalScoringVariables.EMPTY)
    val clinicalScoringFlow: StateFlow<ClinicalScoringVariables> = _clinicalScoringFlow

    init {
        // ✅ Subscribe to telemetryFlow — the only real data pipe
        scope.launch {
            bleManager.telemetryFlow.collect { packet ->
                onBleCharacteristicChanged(packet.data, packet.isLeft, packet.deviceAddress)
            }
        }

        // ✅ Observe hourly steps from service
        scope.launch {
            stepCounterService.hourlyStepsFlow.collect { hourly ->
                _hourlyStepsFlow.value = hourly
            }
        }

        // ✅ Observe total steps from service so aggregate flow reflects resets & step changes immediately
        scope.launch {
            stepCounterService.totalStepsFlow.collect {
                packetMutex.withLock {
                    _sensorDataFlow.value = aggregateSensorData()
                }
            }
        }

        // ✅ Load today's existing row from Room if present (e.g. across app restarts) and prune old records
        scope.launch {
            packetMutex.withLock {
                loadTodayMetricsLocked()
            }
            try {
                dailyFootMetricDao.deleteOldMetrics(62)
            } catch (e: Exception) {
                android.util.Log.e("BLE_DATA_DS", "Failed to clean old metrics: ${e.message}")
            }
        }

        // ✅ Stream-stall watchdog (L4): the FSM's 1500 ms inactivity timer
        // only runs inside process(), so a stalled/disconnected stream would
        // otherwise leave the FSM in WALKING forever. Runs under the packet
        // mutex so FSM mutation stays serialized with packet processing.
        scope.launch { stallWatchdog() }
    }

    private suspend fun stallWatchdog() {
        while (true) {
            delay(STALL_WATCHDOG_INTERVAL_MS)
            packetMutex.withLock {
                val now = android.os.SystemClock.elapsedRealtime()
                val leftActive = lastLeftPacketMs != 0L && now - lastLeftPacketMs <= STALE_PACKET_TIMEOUT_MS
                val rightActive = lastRightPacketMs != 0L && now - lastRightPacketMs <= STALE_PACKET_TIMEOUT_MS

                var stalledChanged = false
                if (lastLeftPacketMs != 0L && !leftActive) {
                    stepCounterService.onStreamStalled(isLeft = true)
                    lastLeftPacketMs = 0L
                    currentLeftPacket = null
                    stalledChanged = true
                }
                if (lastRightPacketMs != 0L && !rightActive) {
                    stepCounterService.onStreamStalled(isLeft = false)
                    lastRightPacketMs = 0L
                    currentRightPacket = null
                    stalledChanged = true
                }
                if (stalledChanged) {
                    _sensorDataFlow.value = aggregateSensorData()
                }

                // If either foot is actively streaming packets, accumulate wear seconds
                if (leftActive || rightActive) {
                    dailyWearSeconds++
                }

                // If clean on-body wear is active, accumulate clean wear seconds
                val walkState = stepCounterService.walkState
                val leftClean = leftActive && (currentLeftPacket?.let { WearGating.isCleanOnBody(it, walkState) } ?: false)
                val rightClean = rightActive && (currentRightPacket?.let { WearGating.isCleanOnBody(it, walkState) } ?: false)
                if (leftClean || rightClean) {
                    dailyCleanWearSeconds++
                }

                // Update 2-day consecutive thermal tracking and clinical variables flow
                updateConsecutiveThermalTrackingLocked()
                _clinicalScoringFlow.value = buildClinicalVariablesLocked(leftClean || rightClean)

                // Check calendar date change at midnight
                val today = LocalDate.now().toString()
                if (today != activeDate) {
                    flushDailyMetricsLocked()
                    activeDate = today
                    resetDailyAccumulatorLocked()
                    loadTodayMetricsLocked()
                    stepCounterService.checkDailyReset(today)
                    _sensorDataFlow.value = aggregateSensorData()
                }

                // Periodic flush every 60 seconds
                watchdogTicksSinceLastFlush++
                if (watchdogTicksSinceLastFlush >= 60) {
                    watchdogTicksSinceLastFlush = 0
                    flushDailyMetricsLocked()
                }
            }
        }
    }

    private suspend fun onBleCharacteristicChanged(rawData: ByteArray, isLeft: Boolean, address: String) = packetMutex.withLock {
        val startNs = System.nanoTime()
        val dataString = String(rawData, Charsets.UTF_8)

        android.util.Log.d("BLE_DATA_DS", "${if (isLeft) "LEFT" else "RIGHT"} [$address] raw: '$dataString'")

        val packets = SensorPacketParser.parseBatch(dataString)
        val parseNs = System.nanoTime() - startNs
        val parseOk = packets.isNotEmpty()
        android.util.Log.i("DIABFOOT_TEST", "PACKET_PROCESS side=${if (isLeft) "LEFT" else "RIGHT"} addr=$address parse_ok=$parseOk packet_count=${packets.size} parse_us=${parseNs / 1000} len=${rawData.size}")

        if (packets.isEmpty()) {
            android.util.Log.e("BLE_DATA_DS", "  Failed to parse any packet from $address")
            return@withLock
        }

        for (sensorPacket in packets) {
            // Temperature-only heartbeat ("T,tempC[,batteryPercent]"): refresh
            // the latest per-foot temperature / battery and merge into the session
            // peaks — but do NOT touch the stream packets, the FSM, the raw flows,
            // or the stall watchdog. A 1/min heartbeat line must never mask a
            // stalled 5 Hz stream.
            if (sensorPacket.isTemperatureOnly) {
                val temp = sensorPacket.temperature
                if (temp > 0f && temp != -999f) {
                    if (isLeft) {
                        latestLeftTempC = temp
                        if (temp > leftPeakTemp) leftPeakTemp = temp
                        accumulateLeftTempLocked(temp)
                        if (WearGating.isPhysiologicalSkinTemp(temp)) {
                            accumulateCleanLeftTempLocked(temp)
                        }
                    } else {
                        latestRightTempC = temp
                        if (temp > rightPeakTemp) rightPeakTemp = temp
                        accumulateRightTempLocked(temp)
                        if (WearGating.isPhysiologicalSkinTemp(temp)) {
                            accumulateCleanRightTempLocked(temp)
                        }
                    }
                }
                sensorPacket.batteryPercent?.let { latestBatteryPercent = it }
                continue
            }

            // Stamp the watchdog (L4) — stream packets only. elapsedRealtime
            // matches the FSM's Clock domain.
            val nowMs = android.os.SystemClock.elapsedRealtime()
            if (isLeft) lastLeftPacketMs = nowMs else lastRightPacketMs = nowMs

            // L1 unit handler: normalize ONLY when the IMU is valid — an
            // all-zero IMU packet (MPU absent) must not pollute the
            // gravity-median unit detection with 0-magnitude samples.
            val normalizedPacket = if (sensorPacket.imuValid) normalizePacket(sensorPacket) else sensorPacket
            if (unitDetector.isDecided && !unitProfileLogged) {
                unitProfileLogged = true
                android.util.Log.i("BLE_DATA_DS", "Sensor unit profile locked: ${unitDetector.profile}")
            }

            android.util.Log.d("BLE_DATA_DS", "  Parsed → Temp=${normalizedPacket.temperature}, Press=${normalizedPacket.pressure}")

            // Accumulate daily thermal samples (TD-7)
            val streamTemp = normalizedPacket.temperature
            if (streamTemp > 0f && streamTemp != -999f) {
                if (isLeft) {
                    accumulateLeftTempLocked(streamTemp)
                } else {
                    accumulateRightTempLocked(streamTemp)
                }
            }

            // Wear-gating & clean data processing (Armstrong / IWGDF standard)
            val walkState = stepCounterService.walkState
            val isCleanWear = WearGating.isCleanOnBody(normalizedPacket, walkState)
            val isActiveStance = WearGating.isActiveStance(normalizedPacket, walkState)

            // Hourly rollover check for clean stance pressure
            val nowHour = LocalTime.now().hour
            if (nowHour != currentHour) {
                currentHour = nowHour
                currentHourStancePressureSum = 0.0
                currentHourStanceSamples = 0
            }

            // Accumulate clean daily thermal samples (noise-filtered skin range 27.0 - 38.5°C)
            if (isCleanWear && WearGating.isPhysiologicalSkinTemp(streamTemp)) {
                if (isLeft) {
                    accumulateCleanLeftTempLocked(streamTemp)
                } else {
                    accumulateCleanRightTempLocked(streamTemp)
                }
            }

            // Accumulate daily peak pressures in kPa (TD-7)
            val maxKpa = normalizedPacket.pressuresKpa.maxOrNull() ?: (if (normalizedPacket.pressure > 0) normalizedPacket.pressure.toFloat() else 0f)
            if (maxKpa > 0f) {
                if (isLeft && maxKpa > dailyLeftPeakPressureKpa) {
                    dailyLeftPeakPressureKpa = maxKpa
                } else if (!isLeft && maxKpa > dailyRightPeakPressureKpa) {
                    dailyRightPeakPressureKpa = maxKpa
                }
            }

            // Accumulate clean stance pressure (averaged only during weight-bearing stance, not swing/idle zeros)
            if (isCleanWear && isActiveStance) {
                val stanceKpa = if (maxKpa > 0f) maxKpa else normalizedPacket.pressure.toFloat()
                if (stanceKpa >= SensorConstants.MIN_STANCE_PRESSURE_KPA) {
                    dailyCleanStancePressureSumKpa += stanceKpa.toDouble()
                    dailyCleanStanceSamples++
                    currentHourStancePressureSum += stanceKpa.toDouble()
                    currentHourStanceSamples++
                }
            }

            if (isLeft) {
                currentLeftPacket = normalizedPacket
                if (normalizedPacket.temperature > leftPeakTemp) {
                    leftPeakTemp = normalizedPacket.temperature
                }
                if (normalizedPacket.pressure.toInt() > leftPeakPressure) {
                    leftPeakPressure = normalizedPacket.pressure.toInt()
                }
            } else {
                currentRightPacket = normalizedPacket
                if (normalizedPacket.temperature > rightPeakTemp) {
                    rightPeakTemp = normalizedPacket.temperature
                }
                if (normalizedPacket.pressure.toInt() > rightPeakPressure) {
                    rightPeakPressure = normalizedPacket.pressure.toInt()
                }
            }

            // Update domain-level step counter via use case — only for packets
            // with a real IMU sample; zero-IMU packets must not create phantom
            // steps in the FSM.
            if (normalizedPacket.imuValid) {
                processStepCountUseCase(normalizedPacket, isLeft)
            }
        }

        if (isLeft) {
            _rawLeftDataFlow.value = dataString
        } else {
            _rawRightDataFlow.value = dataString
        }

        // Broadcast aggregated state
        _sensorDataFlow.value = aggregateSensorData()
    }

    /**
     * L1 unit handler: converts the firmware payload into the canonical
     * internal units (g, deg/s) exactly once, right after parsing. With the
     * g profile this is an identity pass-through, so today's POC behaviour
     * is unchanged; an m/s² firmware is converted here instead of being
     * misread by the FSM.
     */
    private fun normalizePacket(packet: SensorPacket): SensorPacket {
        val profile = unitDetector.observe(packet)
        if (profile == SensorUnitsProfile.MPU6050_G_DEGS) return packet
        return packet.copy(
            accelX = profile.normalizeAccel(packet.accelX),
            accelY = profile.normalizeAccel(packet.accelY),
            accelZ = profile.normalizeAccel(packet.accelZ),
            gyroX = profile.normalizeGyro(packet.gyroX),
            gyroY = profile.normalizeGyro(packet.gyroY),
            gyroZ = profile.normalizeGyro(packet.gyroZ)
        )
    }

    /**
     * Resets session-specific statistics like peak temperatures.
     * Runs under the packet mutex on the IO scope — the peak/packet fields
     * are owned by the telemetry collector coroutine. Flushes daily metrics first.
     */
    fun resetSessionStats() {
        scope.launch {
            packetMutex.withLock {
                flushDailyMetricsLocked()
                leftPeakTemp = 0f
                rightPeakTemp = 0f
                leftPeakPressure = 0
                rightPeakPressure = 0
                currentLeftPacket = null
                currentRightPacket = null
                latestLeftTempC = 0f
                latestRightTempC = 0f
                latestBatteryPercent = null
                stepCounterService.reset()
                unitDetector.reset()
                unitProfileLogged = false
                lastLeftPacketMs = 0L
                lastRightPacketMs = 0L
                _sensorDataFlow.value = aggregateSensorData()
                lastRightPacketMs = 0L
            }
        }
    }

    private fun accumulateLeftTempLocked(temp: Float) {
        if (dailyLeftSamples == 0 || temp < dailyLeftMinTemp) dailyLeftMinTemp = temp
        if (temp > dailyLeftMaxTemp) dailyLeftMaxTemp = temp
        dailyLeftSumTemp += temp.toDouble()
        dailyLeftSamples++
    }

    private fun accumulateRightTempLocked(temp: Float) {
        if (dailyRightSamples == 0 || temp < dailyRightMinTemp) dailyRightMinTemp = temp
        if (temp > dailyRightMaxTemp) dailyRightMaxTemp = temp
        dailyRightSumTemp += temp.toDouble()
        dailyRightSamples++
    }

    private fun accumulateCleanLeftTempLocked(temp: Float) {
        if (dailyCleanLeftSamples == 0 || temp < dailyCleanLeftMinTemp) dailyCleanLeftMinTemp = temp
        if (temp > dailyCleanLeftMaxTemp) dailyCleanLeftMaxTemp = temp
        dailyCleanLeftSumTemp += temp.toDouble()
        dailyCleanLeftSamples++
    }

    private fun accumulateCleanRightTempLocked(temp: Float) {
        if (dailyCleanRightSamples == 0 || temp < dailyCleanRightMinTemp) dailyCleanRightMinTemp = temp
        if (temp > dailyCleanRightMaxTemp) dailyCleanRightMaxTemp = temp
        dailyCleanRightSumTemp += temp.toDouble()
        dailyCleanRightSamples++
    }

    private fun updateConsecutiveThermalTrackingLocked() {
        val cleanLeftAvg = if (dailyCleanLeftSamples > 0) (dailyCleanLeftSumTemp / dailyCleanLeftSamples).toFloat() else 0f
        val cleanRightAvg = if (dailyCleanRightSamples > 0) (dailyCleanRightSumTemp / dailyCleanRightSamples).toFloat() else 0f
        val hasBilateral = dailyCleanLeftSamples >= 10 && dailyCleanRightSamples >= 10

        if (hasBilateral) {
            val diff = kotlin.math.abs(cleanLeftAvg - cleanRightAvg)
            if (diff >= SensorConstants.TEMPERATURE_DIFF_WARNING) {
                val currentWarmer = if (cleanLeftAvg >= cleanRightAvg) FootSide.LEFT else FootSide.RIGHT
                if (yesterdayWarmerFoot == currentWarmer && yesterdayThermalAsymmetryC >= SensorConstants.TEMPERATURE_DIFF_WARNING) {
                    consecutiveThermalDays = maxOf(2, consecutiveThermalDays)
                    sustainedThermalSide = currentWarmer.name
                } else {
                    if (consecutiveThermalDays < 2) {
                        consecutiveThermalDays = 1
                        sustainedThermalSide = currentWarmer.name
                    }
                }
            } else {
                consecutiveThermalDays = 0
                sustainedThermalSide = null
            }
        }
    }

    private fun buildClinicalVariablesLocked(isCurrentlyWornClean: Boolean): ClinicalScoringVariables {
        val cleanLeftAvg = if (dailyCleanLeftSamples > 0) (dailyCleanLeftSumTemp / dailyCleanLeftSamples).toFloat() else 0f
        val cleanRightAvg = if (dailyCleanRightSamples > 0) (dailyCleanRightSumTemp / dailyCleanRightSamples).toFloat() else 0f
        val cleanAsymmetry = if (dailyCleanLeftSamples > 0 && dailyCleanRightSamples > 0) {
            kotlin.math.abs(cleanLeftAvg - cleanRightAvg)
        } else 0f
        val warmer = when {
            cleanLeftAvg > cleanRightAvg -> FootSide.LEFT
            cleanRightAvg > cleanLeftAvg -> FootSide.RIGHT
            else -> null
        }

        val walkState = stepCounterService.walkState
        val leftActiveStance = currentLeftPacket?.let { WearGating.isActiveStance(it, walkState) } ?: false
        val rightActiveStance = currentRightPacket?.let { WearGating.isActiveStance(it, walkState) } ?: false

        val currentHourMean = if (currentHourStanceSamples > 0) {
            (currentHourStancePressureSum / currentHourStanceSamples).toFloat()
        } else 0f

        val dailyMean = if (dailyCleanStanceSamples > 0) {
            (dailyCleanStancePressureSumKpa / dailyCleanStanceSamples).toFloat()
        } else 0f

        val maxPeak = maxOf(dailyLeftPeakPressureKpa, dailyRightPeakPressureKpa)
        val minPeak = minOf(dailyLeftPeakPressureKpa, dailyRightPeakPressureKpa)
        val pressureRatio = if (maxPeak > 0f) (maxPeak - minPeak) / maxPeak else 0f

        val thermalExceed = if (consecutiveThermalDays >= 2 || (dailyCleanLeftSamples >= 10 && dailyCleanRightSamples >= 10 && cleanAsymmetry >= SensorConstants.TEMPERATURE_DIFF_WARNING)) 1 else 0
        val pressureExceed = if (dailyLeftPeakPressureKpa >= SensorConstants.PRESSURE_THRESHOLD_WARNING ||
            dailyRightPeakPressureKpa >= SensorConstants.PRESSURE_THRESHOLD_WARNING ||
            currentHourMean >= SensorConstants.PRESSURE_THRESHOLD_WARNING) 1 else 0
        val (risk, isValidSwell) = ClinicalScoringVariables.calculateRisk(thermalExceed, pressureExceed)

        return ClinicalScoringVariables(
            date = activeDate,
            cleanWearMinutes = (dailyCleanWearSeconds / 60L).toInt(),
            isCurrentlyWornClean = isCurrentlyWornClean,
            isCurrentlyActiveStance = leftActiveStance || rightActiveStance,
            cleanLeftAvgTemp = cleanLeftAvg,
            cleanRightAvgTemp = cleanRightAvg,
            cleanThermalAsymmetryC = cleanAsymmetry,
            warmerFoot = warmer,
            cleanLeftTempSamples = dailyCleanLeftSamples,
            cleanRightTempSamples = dailyCleanRightSamples,
            consecutiveThermalDays = consecutiveThermalDays,
            sustainedThermalSide = sustainedThermalSide?.let { runCatching { FootSide.valueOf(it) }.getOrNull() },
            isSustainedHotspot2Day = consecutiveThermalDays >= 2 && !sustainedThermalSide.isNullOrBlank(),
            yesterdayCleanThermalAsymmetryC = yesterdayThermalAsymmetryC,
            yesterdayWarmerFoot = yesterdayWarmerFoot,
            currentHourCleanStanceMeanPressureKpa = currentHourMean,
            currentHourStanceSamples = currentHourStanceSamples,
            dailyCleanStanceMeanPressureKpa = dailyMean,
            dailyCleanStanceSamples = dailyCleanStanceSamples,
            cleanLeftPeakPressureKpa = dailyLeftPeakPressureKpa,
            cleanRightPeakPressureKpa = dailyRightPeakPressureKpa,
            pressureAsymmetryRatio = pressureRatio,
            isThermalDataSufficient = dailyCleanLeftSamples >= 10 && dailyCleanRightSamples >= 10,
            isPressureDataSufficient = currentHourStanceSamples >= 10,
            thermalExceed = thermalExceed,
            pressureExceed = pressureExceed,
            isValidSwell = isValidSwell,
            riskLevel = risk
        )
    }

    private suspend fun loadTodayMetricsLocked() {
        try {
            val existing = dailyFootMetricDao.getMetricsForDate(activeDate)
            if (existing != null) {
                dailyLeftMinTemp = existing.leftMinTemp
                dailyLeftMaxTemp = existing.leftMaxTemp
                dailyLeftSumTemp = existing.leftSumTemp
                dailyLeftSamples = existing.leftSamples
                dailyRightMinTemp = existing.rightMinTemp
                dailyRightMaxTemp = existing.rightMaxTemp
                dailyRightSumTemp = existing.rightSumTemp
                dailyRightSamples = existing.rightSamples
                dailyLeftPeakPressureKpa = existing.leftPeakPressureKpa
                dailyRightPeakPressureKpa = existing.rightPeakPressureKpa
                dailyWearSeconds = existing.wearMinutes.toLong() * 60L

                dailyCleanLeftMinTemp = existing.cleanLeftMinTemp
                dailyCleanLeftMaxTemp = existing.cleanLeftMaxTemp
                dailyCleanLeftSumTemp = existing.cleanLeftSumTemp
                dailyCleanLeftSamples = existing.cleanLeftSamples
                dailyCleanRightMinTemp = existing.cleanRightMinTemp
                dailyCleanRightMaxTemp = existing.cleanRightMaxTemp
                dailyCleanRightSumTemp = existing.cleanRightSumTemp
                dailyCleanRightSamples = existing.cleanRightSamples
                dailyCleanWearSeconds = existing.cleanWearMinutes.toLong() * 60L
                dailyCleanStancePressureSumKpa = existing.cleanStancePressureSumKpa
                dailyCleanStanceSamples = existing.cleanStanceSamples
                consecutiveThermalDays = existing.consecutiveThermalDays
                sustainedThermalSide = existing.sustainedThermalSide
                android.util.Log.d("BLE_DATA_DS", "Loaded daily metrics for $activeDate: ${existing.wearMinutes} min wear, clean=${existing.cleanWearMinutes}m")
            }

            // Load yesterday's metrics for 2-day consecutive comparison
            loadYesterdayMetricsLocked()
        } catch (e: Exception) {
            android.util.Log.e("BLE_DATA_DS", "Failed to load today's daily metrics: ${e.message}")
        }
    }

    private suspend fun loadYesterdayMetricsLocked() {
        val yesterday = LocalDate.now().minusDays(1).toString()
        val yRecord = dailyFootMetricDao.getMetricsForDate(yesterday)
        if (yRecord != null) {
            yesterdayThermalAsymmetryC = if (yRecord.cleanLeftSamples > 0 && yRecord.cleanRightSamples > 0) {
                yRecord.cleanThermalAsymmetryC
            } else {
                yRecord.thermalAsymmetryC
            }
            val yLeft = if (yRecord.cleanLeftSamples > 0) yRecord.cleanLeftAvgTemp else yRecord.leftAvgTemp
            val yRight = if (yRecord.cleanRightSamples > 0) yRecord.cleanRightAvgTemp else yRecord.rightAvgTemp
            yesterdayWarmerFoot = when {
                yLeft > yRight -> FootSide.LEFT
                yRight > yLeft -> FootSide.RIGHT
                else -> null
            }
        } else {
            yesterdayThermalAsymmetryC = 0f
            yesterdayWarmerFoot = null
        }
    }

    /**
     * Public API for diagnostics/scenario seeder to refresh yesterday's baseline immediately.
     */
    override fun reloadYesterdayMetrics() {
        scope.launch {
            packetMutex.withLock {
                loadYesterdayMetricsLocked()
                updateConsecutiveThermalTrackingLocked()
                val walkState = stepCounterService.walkState
                val leftClean = currentLeftPacket?.let { WearGating.isCleanOnBody(it, walkState) } ?: false
                val rightClean = currentRightPacket?.let { WearGating.isCleanOnBody(it, walkState) } ?: false
                _clinicalScoringFlow.value = buildClinicalVariablesLocked(leftClean || rightClean)
            }
        }
    }

    private suspend fun flushDailyMetricsLocked() {
        if (dailyLeftSamples > 0 || dailyRightSamples > 0 || dailyWearSeconds > 0L || dailyCleanWearSeconds > 0L) {
            val entity = DailyFootMetricEntity(
                date = activeDate,
                leftMinTemp = dailyLeftMinTemp,
                leftMaxTemp = dailyLeftMaxTemp,
                leftSumTemp = dailyLeftSumTemp,
                leftSamples = dailyLeftSamples,
                rightMinTemp = dailyRightMinTemp,
                rightMaxTemp = dailyRightMaxTemp,
                rightSumTemp = dailyRightSumTemp,
                rightSamples = dailyRightSamples,
                leftPeakPressureKpa = dailyLeftPeakPressureKpa,
                rightPeakPressureKpa = dailyRightPeakPressureKpa,
                wearMinutes = (dailyWearSeconds / 60L).toInt(),
                cleanLeftMinTemp = dailyCleanLeftMinTemp,
                cleanLeftMaxTemp = dailyCleanLeftMaxTemp,
                cleanLeftSumTemp = dailyCleanLeftSumTemp,
                cleanLeftSamples = dailyCleanLeftSamples,
                cleanRightMinTemp = dailyCleanRightMinTemp,
                cleanRightMaxTemp = dailyCleanRightMaxTemp,
                cleanRightSumTemp = dailyCleanRightSumTemp,
                cleanRightSamples = dailyCleanRightSamples,
                cleanWearMinutes = (dailyCleanWearSeconds / 60L).toInt(),
                cleanStancePressureSumKpa = dailyCleanStancePressureSumKpa,
                cleanStanceSamples = dailyCleanStanceSamples,
                consecutiveThermalDays = consecutiveThermalDays,
                sustainedThermalSide = sustainedThermalSide
            )
            try {
                dailyFootMetricDao.insertOrUpdateMetrics(entity)
                android.util.Log.d("BLE_DATA_DS", "Flushed daily metrics for $activeDate: wear=${entity.wearMinutes}m cleanWear=${entity.cleanWearMinutes}m consecutiveDays=$consecutiveThermalDays")
            } catch (e: Exception) {
                android.util.Log.e("BLE_DATA_DS", "Failed to flush daily metrics: ${e.message}")
            }
        }
    }

    private fun resetDailyAccumulatorLocked() {
        dailyLeftMinTemp = 0f
        dailyLeftMaxTemp = 0f
        dailyLeftSumTemp = 0.0
        dailyLeftSamples = 0
        dailyRightMinTemp = 0f
        dailyRightMaxTemp = 0f
        dailyRightSumTemp = 0.0
        dailyRightSamples = 0
        dailyLeftPeakPressureKpa = 0f
        dailyRightPeakPressureKpa = 0f
        dailyWearSeconds = 0L

        dailyCleanLeftMinTemp = 0f
        dailyCleanLeftMaxTemp = 0f
        dailyCleanLeftSumTemp = 0.0
        dailyCleanLeftSamples = 0
        dailyCleanRightMinTemp = 0f
        dailyCleanRightMaxTemp = 0f
        dailyCleanRightSumTemp = 0.0
        dailyCleanRightSamples = 0
        dailyCleanWearSeconds = 0L
        dailyCleanStancePressureSumKpa = 0.0
        dailyCleanStanceSamples = 0
        currentHourStancePressureSum = 0.0
        currentHourStanceSamples = 0
        consecutiveThermalDays = 0
        sustainedThermalSide = null
    }

    private fun aggregateSensorData(): InsoleSensorData {
        val left = currentLeftPacket
        val right = currentRightPacket

        val leftPressure = left?.pressure?.toInt() ?: 0
        val rightPressure = right?.pressure?.toInt() ?: 0

        // Per-foot temperature: prefer the latest NTC heartbeat reading (v2);
        // fall back to the stream packet's temperature (legacy v1 lines carry
        // it inline). 0 means "no reading yet" in both cases.
        val leftTemp = if (latestLeftTempC > 0f) latestLeftTempC else left?.temperature ?: 0f
        val rightTemp = if (latestRightTempC > 0f) latestRightTempC else right?.temperature ?: 0f

        return InsoleSensorData(
            pressureValues = listOf(leftPressure, rightPressure),
            temperature = (leftTemp + rightTemp) / if (leftTemp > 0 && rightTemp > 0) 2f else 1f,
            leftTemperature = leftTemp,
            rightTemperature = rightTemp,
            leftPeakTemp = leftPeakTemp,
            rightPeakTemp = rightPeakTemp,
            leftPressure = leftPressure,
            rightPressure = rightPressure,
            leftPeakPressure = leftPeakPressure,
            rightPeakPressure = rightPeakPressure,
            stepCount = stepCounterService.totalSteps,
            leftSteps = stepCounterService.leftSteps,
            rightSteps = stepCounterService.rightSteps,
            walkState = stepCounterService.walkState,
            combinedAccelMag = stepCounterService.combinedAccelMag(
                leftPresent = left != null,
                rightPresent = right != null
            ),
            // Real battery percent when the v2 heartbeat reports one; 0 indicates
            // hardware battery divider is not installed (TD-13) so UI shows "Active".
            batteryLevel = latestBatteryPercent ?: 0,
            timestamp = System.currentTimeMillis(),
            leftZonePressuresKpa = left?.pressuresKpa ?: emptyList(),
            rightZonePressuresKpa = right?.pressuresKpa ?: emptyList(),
        )
    }
}
