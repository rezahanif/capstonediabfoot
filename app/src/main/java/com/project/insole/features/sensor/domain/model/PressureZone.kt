package com.project.insole.features.sensor.domain.model

/**
 * Represents a pressure zone on the insole.
 * Maps sensor data to medial, lateral, and heel coordinate positions.
 */
data class PressureZone(
    val x: Int,              // X coordinate (0-indexed from left medial)
    val y: Int,              // Y coordinate (0-indexed from heel to toe)
    val pressure: Int,       // Pressure value in a normalized range
    val zone: ZoneType       // Classification of the zone
)

enum class ZoneType {
    HEEL,
    MEDIAL_ARCH,
    LATERAL_ARCH,
    METATARSAL_HEAD,
    TOE
}
