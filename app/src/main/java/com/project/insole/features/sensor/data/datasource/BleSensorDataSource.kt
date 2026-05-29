package com.project.insole.features.sensor.data.datasource

import android.annotation.SuppressLint
import com.project.insole.core.ble.InsoleBleManager
import com.project.insole.features.sensor.domain.model.InsoleSensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Parses raw bytes from ESP32 into Kotlin data classes.
 * Connected to InsoleBleManager to receive real BLE data.
 * This is the ONLY place where BLE byte parsing happens.
 * All other layers receive clean domain models.
 */
@SuppressLint("MissingPermission")
class BleSensorDataSource @Inject constructor(
    private val bleManager: InsoleBleManager
) {

    private val _sensorDataFlow = MutableStateFlow<InsoleSensorData?>(null)
    val sensorDataFlow: Flow<InsoleSensorData?> = _sensorDataFlow

    init {
        // Automatically start listening to BLE data when this source is created
        bleManager.setOnCharacteristicChangedListener { rawData ->
            onBleCharacteristicChanged(rawData)
        }
    }

    /**
     * Parses raw BLE characteristic data (Comma-separated String) into sensor readings.
     * Format: "accX,accY,accZ,gyroX,gyroY,gyroZ,pressure,temperature"
     */
    fun parseRawBleData(rawData: ByteArray): InsoleSensorData {
        val dataString = String(rawData, Charsets.UTF_8)
        val parts = dataString.split(",")
        
        // Default values if parsing fails or string is incomplete
        var pressure = 0f
        var temperature = 0f
        
        if (parts.size >= 8) {
            pressure = parts[6].toFloatOrNull() ?: 0f
            temperature = parts[7].toFloatOrNull() ?: 0f
        }

        return InsoleSensorData(
            pressureValues = listOf(pressure.toInt()),
            temperature = temperature,
            leftTemperature = temperature,
            rightTemperature = 0f,
            leftPressure = pressure.toInt(),
            rightPressure = 0,
            stepCount = 0,
            batteryLevel = 100,
            timestamp = System.currentTimeMillis()
        )
    }

    fun onBleCharacteristicChanged(rawData: ByteArray) {
        val sensorData = parseRawBleData(rawData)
        _sensorDataFlow.value = sensorData
    }
}
