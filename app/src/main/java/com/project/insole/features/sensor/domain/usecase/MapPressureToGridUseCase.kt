package com.project.insole.features.sensor.domain.usecase

import com.project.insole.features.sensor.domain.model.InsoleSensorData
import com.project.insole.features.sensor.domain.model.PressureZone
import com.project.insole.features.sensor.domain.model.ZoneType
import javax.inject.Inject

/**
 * Pure Kotlin use case for converting sensor values to a 2D matrix for UI visualization.
 * No Android or BLE dependencies - just data transformation logic.
 */
class MapPressureToGridUseCase @Inject constructor() {

    private val GRID_WIDTH = 5   // 5 columns (medial to lateral)
    private val GRID_HEIGHT = 10  // 10 rows (heel to toe)

    /**
     * Converts flat sensor pressure list into a 2D grid for heatmap visualization.
     */
    operator fun invoke(sensorData: InsoleSensorData): List<List<PressureZone>> {
        val grid = mutableListOf<List<PressureZone>>()

        for (y in 0 until GRID_HEIGHT) {
            val row = mutableListOf<PressureZone>()
            for (x in 0 until GRID_WIDTH) {
                val index = (y * GRID_WIDTH) + x
                val pressure = if (index < sensorData.pressureValues.size) {
                    sensorData.pressureValues[index]
                } else {
                    0
                }

                val zone = determinePressureZone(x, y)
                row.add(
                    PressureZone(
                        x = x,
                        y = y,
                        pressure = pressure,
                        zone = zone
                    )
                )
            }
            grid.add(row)
        }

        return grid
    }

    private fun determinePressureZone(x: Int, y: Int): ZoneType {
        return when {
            y < 2 -> ZoneType.HEEL
            y < 4 -> if (x < 2 || x > 2) ZoneType.LATERAL_ARCH else ZoneType.MEDIAL_ARCH
            y < 7 -> if (x < 2 || x > 2) ZoneType.LATERAL_ARCH else ZoneType.MEDIAL_ARCH
            y < 9 -> ZoneType.METATARSAL_HEAD
            else -> ZoneType.TOE
        }
    }
}
