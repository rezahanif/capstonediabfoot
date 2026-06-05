package com.project.insole.features.sensor.domain.model

/**
 * Pure Kotlin data class representing flattened sensor readings from the insole.
 * This is part of the domain layer and has NO Android dependencies.
 */
data class InsoleSensorData(
    val pressureValues: List<Int>,  // Raw pressure readings from multiple zones
    val temperature: Float,         // Average temperature in Celsius
    val leftTemperature: Float,    // Temperature on left foot
    val rightTemperature: Float,   // Temperature on right foot
    val leftPressure: Int,         // Aggregated pressure reading for left foot (0-255)
    val rightPressure: Int,        // Aggregated pressure reading for right foot (0-255)
    val stepCount: Int,            // Step count
    val batteryLevel: Int,         // Battery percentage (0-100)
    val timestamp: Long            // UTC timestamp in milliseconds
)
