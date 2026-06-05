package com.project.insole.features.sensor.data.datasource

import android.annotation.SuppressLint
import com.project.insole.core.ble.InsoleBleManager
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Collects raw BLE data from InsoleBleManager and aggregates it into domain models.
 */
@SuppressLint("MissingPermission")
@Singleton
class BleSensorDataSource @Inject constructor(
    private val bleManager: InsoleBleManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sensorDataFlow = MutableStateFlow<InsoleSensorData?>(null)
    val sensorDataFlow: Flow<InsoleSensorData?> = _sensorDataFlow

    private val _rawLeftDataFlow = MutableStateFlow<String?>(null)
    val rawLeftDataFlow: Flow<String?> = _rawLeftDataFlow

    private val _rawRightDataFlow = MutableStateFlow<String?>(null)
    val rawRightDataFlow: Flow<String?> = _rawRightDataFlow

    private var currentLeftData: String? = null
    private var currentRightData: String? = null

    init {
        // Subscribe to telemetryFlow - the real data pipe from InsoleBleManager
        scope.launch {
            bleManager.telemetryFlow.collect { packet ->
                onBleCharacteristicChanged(packet.data, packet.isLeft)
            }
        }
    }

    private fun onBleCharacteristicChanged(rawData: ByteArray, isLeft: Boolean) {
        val dataString = String(rawData, Charsets.UTF_8)
        
        if (isLeft) {
            currentLeftData = dataString
            _rawLeftDataFlow.value = dataString
        } else {
            currentRightData = dataString
            _rawRightDataFlow.value = dataString
        }

        val sensorData = aggregateSensorData(currentLeftData, currentRightData)
        _sensorDataFlow.value = sensorData
    }

    private fun aggregateSensorData(leftRaw: String?, rightRaw: String?): InsoleSensorData {
        val leftParts = leftRaw?.split(",") ?: emptyList()
        val rightParts = rightRaw?.split(",") ?: emptyList()

        val leftPressure = if (leftParts.size >= 8) leftParts[6].toFloatOrNull() ?: 0f else 0f
        val rightPressure = if (rightParts.size >= 8) rightParts[6].toFloatOrNull() ?: 0f else 0f
        
        val leftTemp = if (leftParts.size >= 8) leftParts[7].toFloatOrNull() ?: 0f else 0f
        val rightTemp = if (rightParts.size >= 8) rightParts[7].toFloatOrNull() ?: 0f else 0f

        return InsoleSensorData(
            pressureValues = listOf(leftPressure.toInt(), rightPressure.toInt()),
            temperature = (leftTemp + rightTemp) / if (leftTemp > 0 && rightTemp > 0) 2f else 1f,
            leftTemperature = leftTemp,
            rightTemperature = rightTemp,
            leftPressure = leftPressure.toInt(),
            rightPressure = rightPressure.toInt(),
            stepCount = 0, // Steps are handled by domain service
            batteryLevel = 100,
            timestamp = System.currentTimeMillis()
        )
    }
}
