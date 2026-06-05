package com.project.insole.core.ble

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages BLE connection lifecycle and initialization.
 * Handles automatic connection to previously paired device or prompts for pairing.
 */
@Singleton
@SuppressLint("MissingPermission")
class BleConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bleManager: InsoleBleManager
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    companion object {
        private const val TAG = "BleConnectionManager"
        private const val SAVED_DEVICE_ADDRESS = "saved_insole_device_address"
        private const val SAVED_DEVICE_NAME = "saved_insole_device_name"
    }

    /**
     * Initialize BLE connection on app startup.
     * Attempts to reconnect to previously paired device.
     */
    fun initializeBleConnection() {
        scope.launch {
            try {
                if (!bleManager.isBluetoothEnabled()) {
                    Log.w(TAG, "Bluetooth is not enabled")
                    return@launch
                }

                // Try to reconnect to saved device
                val savedAddress = getSavedDeviceAddress()
                if (savedAddress != null) {
                    Log.d(TAG, "Attempting to reconnect to saved device: $savedAddress")
                    bleManager.connect(savedAddress)
                } else {
                    Log.d(TAG, "No saved device, starting scan for pairing")
                    bleManager.startScanning()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing BLE connection", e)
            }
        }
    }

    /**
     * Save device connection for automatic reconnection.
     */
    fun saveDeviceConnection(deviceAddress: String, deviceName: String) {
        val sharedPrefs = context.getSharedPreferences("ble_config", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString(SAVED_DEVICE_ADDRESS, deviceAddress)
            putString(SAVED_DEVICE_NAME, deviceName)
            apply()
        }
        Log.d(TAG, "Saved device connection: $deviceAddress - $deviceName")
    }

    /**
     * Get previously saved device address.
     */
    fun getSavedDeviceAddress(): String? {
        val sharedPrefs = context.getSharedPreferences("ble_config", Context.MODE_PRIVATE)
        return sharedPrefs.getString(SAVED_DEVICE_ADDRESS, null)
    }

    /**
     * Get previously saved device name.
     */
    fun getSavedDeviceName(): String? {
        val sharedPrefs = context.getSharedPreferences("ble_config", Context.MODE_PRIVATE)
        return sharedPrefs.getString(SAVED_DEVICE_NAME, null)
    }

    /**
     * Clear saved device connection.
     */
    fun clearSavedDevice() {
        val sharedPrefs = context.getSharedPreferences("ble_config", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            remove(SAVED_DEVICE_ADDRESS)
            remove(SAVED_DEVICE_NAME)
            apply()
        }
        Log.d(TAG, "Cleared saved device connection")
    }

    /**
     * Disconnect from device.
     */
    fun disconnectDevice() {
        scope.launch {
            bleManager.disconnect()
            Log.d(TAG, "Disconnected from device")
        }
    }
}
