package com.project.insole.features.sensor.data.datasource

import android.annotation.SuppressLint
import com.project.insole.core.ble.InsoleBleManager
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.model.SensorPacket
import com.project.insole.features.sensor.domain.service.StepCounterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton source of truth for parsed BLE telemetry.
 * Subscribes to raw telemetryFlow, parses into SensorPackets,
 * and aggregates them into domain-friendly InsoleSensorData.
 */
@SuppressLint("MissingPermission")
@Singleton
class BleSensorDataSource @Inject constructor(
    private val bleManager: InsoleBleManager,
    private val stepCounterService: StepCounterService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sensorDataFlow = MutableStateFlow<InsoleSensorData?>(null)
    val sensorDataFlow: Flow<InsoleSensorData?> = _sensorDataFlow

    private val _rawLeftDataFlow = MutableStateFlow<String?>(null)
    val rawLeftDataFlow: Flow<String?> = _rawLeftDataFlow

    private val _rawRightDataFlow = MutableStateFlow<String?>(null)
    val rawRightDataFlow: Flow<String?> = _rawRightDataFlow

    private var currentLeftPacket: SensorPacket? = null
    private var currentRightPacket: SensorPacket? = null

    init {
        scope.launch {
            bleManager.telemetryFlow.collect { packet ->
                onBleCharacteristicChanged(packet.data, packet.isLeft, packet.deviceAddress)
            }
        }
    }

    private fun onBleCharacteristicChanged(rawData: ByteArray, isLeft: Boolean, address: String) {
        val dataString = String(rawData, Charsets.UTF_8)
        
        android.util.Log.d("BLE_DATA", "${if (isLeft) "LEFT" else "RIGHT"} [$address] raw: '$dataString'")

        val sensorPacket = SensorPacket.fromBleString(dataString)
        if (sensorPacket == null) {
            android.util.Log.e("BLE_DATA", "  Failed to parse packet from $address")
            return
        }

        android.util.Log.d("BLE_DATA", "  Parsed: Temp=${sensorPacket.temperature}, Press=${sensorPacket.pressure}")

        if (isLeft) {
            currentLeftPacket = sensorPacket
            _rawLeftDataFlow.value = dataString
        } else {
            currentRightPacket = sensorPacket
            _rawRightDataFlow.value = dataString
        }

        // Update domain-level step counter
        stepCounterService.processPacket(sensorPacket, isLeft)

        // Broadcast aggregated state
        _sensorDataFlow.value = aggregateSensorData()
    }

    private fun aggregateSensorData(): InsoleSensorData {
        val left = currentLeftPacket
        val right = currentRightPacket

        val leftPressure = left?.pressure?.toInt() ?: 0
        val rightPressure = right?.pressure?.toInt() ?: 0
        
        val leftTemp = left?.temperature ?: 0f
        val rightTemp = right?.temperature ?: 0f

        return InsoleSensorData(
            pressureValues = listOf(leftPressure, rightPressure),
            temperature = (leftTemp + rightTemp) / if (leftTemp > 0 && rightTemp > 0) 2f else 1f,
            leftTemperature = leftTemp,
            rightTemperature = rightTemp,
            leftPressure = leftPressure,
            rightPressure = rightPressure,
            stepCount = stepCounterService.totalSteps,
            walkState = stepCounterService.walkState,
            combinedAccelMag = stepCounterService.combinedAccelMag,
            batteryLevel = 100, // Placeholder
            timestamp = System.currentTimeMillis()
        )
    }
}
