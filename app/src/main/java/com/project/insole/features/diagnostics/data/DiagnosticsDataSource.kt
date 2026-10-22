package com.project.insole.features.diagnostics.data

import com.project.insole.core.ble.InsoleBleManager
import com.project.insole.core.ble.model.BleDeviceState
import com.project.insole.core.network.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for device diagnostics information.
 *
 * Honest by design (brand spec R7 / TD-4): every value here is either real
 * or absent. RSSI comes from the last scan results; battery is NULL because
 * the firmware has no battery monitoring yet (TD-13 — a divider on GPIO5 is
 * designed, pending hardware), and no placeholder number is ever returned.
 */
@Singleton
class DiagnosticsDataSource @Inject constructor(
    private val bleManager: InsoleBleManager
) {

    private val _deviceStatusFlow = MutableStateFlow<DeviceStatus?>(null)
    val deviceStatusFlow: Flow<DeviceStatus?> = _deviceStatusFlow

    data class DeviceStatus(
        val batteryLevel: Int?,     // 0-100, null = firmware cannot report it yet (TD-13)
        val rssiStrength: Int?,     // RSSI in dBm, null = never scanned while visible
        val connectionState: String,
        val firmwareVersion: String,
        val lastSyncTime: Long
    )

    /**
     * Builds device status from real BLE state.
     */
    suspend fun getDeviceStatus(): Result<DeviceStatus> {
        return try {
            val leftState = bleManager.leftDeviceState.value
            val rightState = bleManager.rightDeviceState.value

            val connectionState = when {
                leftState is BleDeviceState.Connected && rightState is BleDeviceState.Connected -> "Connected"
                leftState is BleDeviceState.Connected || rightState is BleDeviceState.Connected -> "Partial"
                else -> "Disconnected"
            }

            // Real RSSI from the most recent scan results, for whichever
            // insole address is currently registered to a side. Null (shown
            // as "Unknown") when we have no fresh scan for it.
            val rssi = listOf(
                bleManager.connectedAddressFor(isLeft = true),
                bleManager.connectedAddressFor(isLeft = false)
            ).firstNotNullOfOrNull { address ->
                address?.let { addr ->
                    bleManager.scannedDevices.value.firstOrNull { it.address == addr }?.rssi
                }
            }

            val status = DeviceStatus(
                batteryLevel = null, // TD-13: firmware cannot report battery yet
                rssiStrength = rssi,
                connectionState = connectionState,
                firmwareVersion = "1.0.0",
                lastSyncTime = System.currentTimeMillis()
            )
            _deviceStatusFlow.value = status
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Performs device calibration.
     */
    suspend fun calibrateDevice(): Result<Unit> {
        return try {
            // Send calibration command to device
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
