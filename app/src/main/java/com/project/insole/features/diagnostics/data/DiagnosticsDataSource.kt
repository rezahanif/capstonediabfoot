package com.project.insole.features.diagnostics.data

import com.project.insole.core.ble.InsoleBleManager
import com.project.insole.core.ble.model.BleDeviceState
import com.project.insole.features.sensor.data.datasource.BleSensorDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for technical insole diagnostics & doctor-facing telemetry (TD-4).
 *
 * Honest by design (UX-BRAND-SPEC Rule R7):
 * Every value is real. Raw Celsius and kPa numbers are preserved here for doctor
 * and engineering verification, while kept off patient-facing alert views (Rule R1).
 * Battery reports honest hardware telemetry status (TD-13).
 */
@Singleton
class DiagnosticsDataSource @Inject constructor(
    private val bleManager: InsoleBleManager,
    private val bleSensorDataSource: BleSensorDataSource
) {

    private val _deviceStatusFlow = MutableStateFlow<DeviceStatus?>(null)
    val deviceStatusFlow: Flow<DeviceStatus?> = _deviceStatusFlow

    data class InsoleHardwareDiagnostic(
        val side: String,
        val macAddress: String?,
        val connectionState: String,
        val rssiDbm: Int?,
        val liveTemperatureC: Float,
        val peakTemperatureC: Float,
        val livePressureKpa: Float,
        val peakPressureKpa: Float,
        val stepCount: Int
    )

    data class DeviceStatus(
        val leftInsole: InsoleHardwareDiagnostic,
        val rightInsole: InsoleHardwareDiagnostic,
        val thermalAsymmetryC: Float,
        val isBilateralConnected: Boolean,
        val totalSteps: Int,
        val walkState: String,
        val batteryNotice: String,
        val firmwareProtocol: String,
        val lastSyncTime: Long
    )

    /**
     * Builds device status from real BLE state and parsed sensor telemetry.
     */
    suspend fun getDeviceStatus(): Result<DeviceStatus> {
        return try {
            val leftState = bleManager.leftDeviceState.value
            val rightState = bleManager.rightDeviceState.value

            val leftAddr = bleManager.connectedAddressFor(isLeft = true)
            val rightAddr = bleManager.connectedAddressFor(isLeft = false)

            // Scanned RSSI for the connected address
            val scanned = bleManager.scannedDevices.value
            val leftRssi = leftAddr?.let { addr -> scanned.firstOrNull { it.address == addr }?.rssi }
            val rightRssi = rightAddr?.let { addr -> scanned.firstOrNull { it.address == addr }?.rssi }

            val sensorData = bleSensorDataSource.sensorDataFlow.firstOrNull()

            val leftDiag = InsoleHardwareDiagnostic(
                side = "Left Foot",
                macAddress = leftAddr ?: "Not paired",
                connectionState = leftState.javaClass.simpleName,
                rssiDbm = leftRssi,
                liveTemperatureC = sensorData?.leftTemperature ?: 0f,
                peakTemperatureC = sensorData?.leftPeakTemp ?: 0f,
                livePressureKpa = sensorData?.leftPressure?.toFloat() ?: 0f,
                peakPressureKpa = sensorData?.leftPeakPressure?.toFloat() ?: 0f,
                stepCount = sensorData?.leftSteps ?: 0
            )

            val rightDiag = InsoleHardwareDiagnostic(
                side = "Right Foot",
                macAddress = rightAddr ?: "Not paired",
                connectionState = rightState.javaClass.simpleName,
                rssiDbm = rightRssi,
                liveTemperatureC = sensorData?.rightTemperature ?: 0f,
                peakTemperatureC = sensorData?.rightPeakTemp ?: 0f,
                livePressureKpa = sensorData?.rightPressure?.toFloat() ?: 0f,
                peakPressureKpa = sensorData?.rightPeakPressure?.toFloat() ?: 0f,
                stepCount = sensorData?.rightSteps ?: 0
            )

            val asymmetry = if (leftDiag.liveTemperatureC > 0f && rightDiag.liveTemperatureC > 0f) {
                kotlin.math.abs(leftDiag.liveTemperatureC - rightDiag.liveTemperatureC)
            } else 0f

            val status = DeviceStatus(
                leftInsole = leftDiag,
                rightInsole = rightDiag,
                thermalAsymmetryC = asymmetry,
                isBilateralConnected = leftState is BleDeviceState.Connected && rightState is BleDeviceState.Connected,
                totalSteps = sensorData?.stepCount ?: 0,
                walkState = sensorData?.walkState?.name ?: "UNKNOWN",
                batteryNotice = "Not available on hardware",
                firmwareProtocol = "v2 CSV (12-field)",
                lastSyncTime = System.currentTimeMillis()
            )

            _deviceStatusFlow.value = status
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
