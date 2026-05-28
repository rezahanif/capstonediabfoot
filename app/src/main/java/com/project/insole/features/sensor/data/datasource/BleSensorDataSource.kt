package com.project.insole.features.sensor.data.datasource

import com.project.insole.features.sensor.domain.model.InsoleSensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Parses raw bytes from ESP32 into Kotlin data classes.
 * This is the ONLY place where BLE byte parsing happens.
 * All other layers receive clean domain models.
 */
class BleSensorDataSource @Inject constructor() {

    private val _sensorDataFlow = MutableStateFlow<InsoleSensorData?>(null)
    val sensorDataFlow: Flow<InsoleSensorData?> = _sensorDataFlow

    /**
     * Parses raw BLE characteristic data into sensor readings.
     */
    fun parseRawBleData(rawData: ByteArray): InsoleSensorData {
        // Example parsing: extract pressure values, temperature, step count from bytes
        val pressure = parsePressure(rawData)
        val temperature = parseTemperature(rawData)
        val stepCount = parseStepCount(rawData)

        return InsoleSensorData(
            pressureValues = pressure,
            temperature = temperature,
            stepCount = stepCount,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun parsePressure(rawData: ByteArray): List<Int> {
        // Parse pressure from bytes
        return listOf()
    }

    private fun parseTemperature(rawData: ByteArray): Float {
        // Parse temperature from bytes
        return 0f
    }

    private fun parseStepCount(rawData: ByteArray): Int {
        // Parse step count from bytes
        return 0
    }

    fun onBleCharacteristicChanged(rawData: ByteArray) {
        val sensorData = parseRawBleData(rawData)
        _sensorDataFlow.value = sensorData
    }
}
