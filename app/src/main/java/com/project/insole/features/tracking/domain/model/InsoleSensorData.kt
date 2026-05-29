package com.project.insole.features.tracking.domain.model

/**
 * Pure Kotlin data class representing flattened sensor readings from the insole.
 * This is part of the domain layer and has NO Android dependencies.
 * Includes separate readings for left and right foot.
 */
data class InsoleSensorData(
    val pressureValues: List<Int>,  // Raw pressure readings from multiple zones
    val temperature: Float,         // Temperature in Celsius (legacy, use leftTemperature or rightTemperature)
    val leftTemperature: Float = temperature,    // Temperature on left foot
    val rightTemperature: Float = temperature,   // Temperature on right foot
    val leftPressure: Int = 0,      // Aggregated pressure reading for left foot
    val rightPressure: Int = 0,     // Aggregated pressure reading for right foot
    val stepCount: Int,            // Step count (may be from device or calculated)
    val batteryLevel: Int = 0,     // Battery percentage (0-100)
    val timestamp: Long            // UTC timestamp in milliseconds
)
