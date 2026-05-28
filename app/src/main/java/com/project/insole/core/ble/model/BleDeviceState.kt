package com.project.insole.core.ble.model

/**
 * Represents the connection state of a BLE device.
 */
sealed class BleDeviceState {
    object Disconnected : BleDeviceState()
    object Connecting : BleDeviceState()
    object Connected : BleDeviceState()
    object Discovering : BleDeviceState()
    data class Error(val message: String) : BleDeviceState()
}
