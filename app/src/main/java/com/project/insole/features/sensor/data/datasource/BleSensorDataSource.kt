package com.project.insole.features.sensor.data.datasource

import android.annotation.SuppressLint
import com.project.insole.core.ble.InsoleBleManager
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/** Silence longer than this (≈10 lost packets at 5 Hz) means one foot's FSM is stale. */
private const val STALE_PACKET_TIMEOUT_MS = 2000L
private const val STALL_WATCHDOG_INTERVAL_MS = 1000L

/**
 * Singleton source of truth for parsed BLE telemetry.
 * Subscribes to raw telemetryFlow, parses into SensorPackets,
 * and aggregates them into domain-friendly InsoleSensorData.
 */
@SuppressLint("MissingPermission")
@Singleton
class BleSensorDataSource @Inject constructor(
    private val bleManager: InsoleBleManager,
    private val stepCounterService: StepCounterService,
    private val processStepCountUseCase: com.project.insole.features.sensor.domain.usecase.ProcessStepCountUseCase
) {
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
                if (lastLeftPacketMs != 0L && now - lastLeftPacketMs > STALE_PACKET_TIMEOUT_MS) {
                    stepCounterService.onStreamStalled(isLeft = true)
                    lastLeftPacketMs = 0L
                }
                if (lastRightPacketMs != 0L && now - lastRightPacketMs > STALE_PACKET_TIMEOUT_MS) {
                    stepCounterService.onStreamStalled(isLeft = false)
                    lastRightPacketMs = 0L
                }
            }
        }
    }

    private suspend fun onBleCharacteristicChanged(rawData: ByteArray, isLeft: Boolean, address: String) = packetMutex.withLock {
        val dataString = String(rawData, Charsets.UTF_8)

        android.util.Log.d("BLE_DATA_DS", "${if (isLeft) "LEFT" else "RIGHT"} [$address] raw: '$dataString'")

        val sensorPacket = SensorPacketParser.parse(dataString)
        if (sensorPacket == null) {
            android.util.Log.e("BLE_DATA_DS", "  Failed to parse packet from $address")
            return@withLock
        }

        // Temperature-only heartbeat ("T,tempC[,batteryPercent]"): refresh
        // the latest per-foot temperature / battery and merge into the session
        // peaks — but do NOT touch the stream packets, the FSM, the raw flows,
        // or the stall watchdog. A 1/min heartbeat line must never mask a
        // stalled 5 Hz stream.
        if (sensorPacket.isTemperatureOnly) {
            if (isLeft) {
                latestLeftTempC = sensorPacket.temperature
                if (sensorPacket.temperature > leftPeakTemp) leftPeakTemp = sensorPacket.temperature
            } else {
                latestRightTempC = sensorPacket.temperature
                if (sensorPacket.temperature > rightPeakTemp) rightPeakTemp = sensorPacket.temperature
            }
            sensorPacket.batteryPercent?.let { latestBatteryPercent = it }
            return@withLock
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

        if (isLeft) {
            currentLeftPacket = normalizedPacket
            if (normalizedPacket.temperature > leftPeakTemp) {
                leftPeakTemp = normalizedPacket.temperature
            }
            if (normalizedPacket.pressure.toInt() > leftPeakPressure) {
                leftPeakPressure = normalizedPacket.pressure.toInt()
            }
            _rawLeftDataFlow.value = dataString
        } else {
            currentRightPacket = normalizedPacket
            if (normalizedPacket.temperature > rightPeakTemp) {
                rightPeakTemp = normalizedPacket.temperature
            }
            if (normalizedPacket.pressure.toInt() > rightPeakPressure) {
                rightPeakPressure = normalizedPacket.pressure.toInt()
            }
            _rawRightDataFlow.value = dataString
        }

        // Update domain-level step counter via use case — only for packets
        // with a real IMU sample; zero-IMU packets must not create phantom
        // steps in the FSM.
        if (normalizedPacket.imuValid) {
            processStepCountUseCase(normalizedPacket, isLeft)
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
     * are owned by the telemetry collector coroutine.
     */
    fun resetSessionStats() {
        scope.launch {
            packetMutex.withLock {
                leftPeakTemp = 0f
                rightPeakTemp = 0f
                leftPeakPressure = 0
                rightPeakPressure = 0
                currentLeftPacket = null
                currentRightPacket = null
                latestLeftTempC = 0f
                latestRightTempC = 0f
                latestBatteryPercent = null
                _sensorDataFlow.value = null
                unitDetector.reset()
                unitProfileLogged = false
                lastLeftPacketMs = 0L
                lastRightPacketMs = 0L
            }
        }
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
            // Real battery percent when the v2 heartbeat reports one; the
            // placeholder 100 stays until firmware battery monitoring exists
            // (TD-13) — no battery hardware in the current POC.
            batteryLevel = latestBatteryPercent ?: 100,
            timestamp = System.currentTimeMillis(),
            leftZonePressuresKpa = left?.pressuresKpa ?: emptyList(),
            rightZonePressuresKpa = right?.pressuresKpa ?: emptyList(),
        )
    }
}
