package com.project.insole.features.tracking.domain.model

/**
 * Pure Kotlin data class representing flattened sensor readings from the insole.
 * This is part of the domain layer and has NO Android dependencies.
 */
data class InsoleSensorData(
    val pressureValues: List<Int>,  // Raw pressure readings from multiple zones
    val temperature: Float,         // Temperature in Celsius
    val stepCount: Int,            // Step count (may be from device or calculated)
    val timestamp: Long            // UTC timestamp in milliseconds
)
