package com.project.insole.features.sensor.domain.model

/**
 * Pure Kotlin data class representing flattened sensor readings from the insole.
 * Consolidated single source of truth for both Left and Right insole data.
 */
data class InsoleSensorData(
    val pressureValues: List<Int>,  // Raw pressure readings (0-255)
    val temperature: Float,         // Average temperature in Celsius
    val leftTemperature: Float,
    val rightTemperature: Float,
    val leftPressure: Int,
    val rightPressure: Int,
    val stepCount: Int,
    val walkState: WalkState,
    val combinedAccelMag: Float,
    val batteryLevel: Int,
    val timestamp: Long
)
