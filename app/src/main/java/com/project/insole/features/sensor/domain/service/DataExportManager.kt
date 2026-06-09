package com.project.insole.features.sensor.domain.service

import com.project.insole.features.sensor.domain.model.InsoleSensorData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Handles generation of CSV reports from sensor history.
 * Currently remains dead code (placeholder).
 */
object DataExportManager {

    private val csvHeader = "Timestamp,WalkState,StepCount,LeftSteps,RightSteps,AvgTemp,LeftTemp,RightTemp,LeftPress,RightPress\n"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Converts a list of sensor data into a single CSV string.
     */
    fun createCsvString(dataList: List<InsoleSensorData>): String {
        val sb = StringBuilder()
        sb.append(csvHeader)
        
        dataList.forEach { data ->
            val row = listOf(
                dateFormat.format(Date(data.timestamp)),
                data.walkState.name,
                data.stepCount.toString(),
                data.leftSteps.toString(),
                data.rightSteps.toString(),
                "%.2f".format(data.temperature),
                "%.2f".format(data.leftTemperature),
                "%.2f".format(data.rightTemperature),
                data.leftPressure.toString(),
                data.rightPressure.toString()
            ).joinToString(",")
            sb.append(row).append("\n")
        }
        
        return sb.toString()
    }
}
