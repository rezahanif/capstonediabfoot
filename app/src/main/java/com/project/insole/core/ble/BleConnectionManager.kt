package com.project.insole.core.ble

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.project.insole.core.ble.model.BleDeviceState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages BLE connection lifecycle and initialization.
 *
 * Persists ONE address per foot (left + right) and reconnects both on app
 * start. On an unexpected disconnect (link loss, insole switched off) each
 * side is retried automatically up to [ReconnectPolicy.MAX_RECONNECTION_ATTEMPTS]
 * times at [ReconnectPolicy.RECONNECTION_INTERVAL_MS] intervals; an
 * app-driven disconnect ("End Session") suppresses retries via
 * [InsoleBleManager.isUserInitiatedDisconnect].
 */
@Singleton
@SuppressLint("MissingPermission")
class BleConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bleManager: InsoleBleManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        private const val TAG = "BleConnectionManager"
        private const val PREFS_NAME = "ble_config"
        private const val SAVED_LEFT_ADDRESS = "saved_left_address"
        private const val SAVED_RIGHT_ADDRESS = "saved_right_address"
        private const val SAVED_LEFT_NAME = "saved_left_name"
        private const val SAVED_RIGHT_NAME = "saved_right_name"
        // Pre-two-slot storage; migrated to the LEFT slot on first launch.
        private const val LEGACY_SAVED_DEVICE_ADDRESS = "saved_insole_device_address"
    }

    private val leftRetryPolicy = ReconnectPolicy()
    private val rightRetryPolicy = ReconnectPolicy()
    private var leftRetryInFlight = false
    private var rightRetryInFlight = false
    private var watchersStarted = false

    /**
     * Initialize BLE connection on app startup.
     * Reconnects both previously paired insoles, or prompts for pairing.
     */
    fun initializeBleConnection() {
        scope.launch {
            try {
                if (!bleManager.isBluetoothEnabled()) {
                    Log.w(TAG, "Bluetooth is not enabled")
                    return@launch
                }

                migrateLegacySingleAddress()
                startReconnectWatchers()

                val leftAddress = getSavedDeviceAddress(isLeft = true)
                val rightAddress = getSavedDeviceAddress(isLeft = false)
                if (leftAddress == null && rightAddress == null) {
                    Log.d(TAG, "No saved devices, starting scan for pairing")
                    bleManager.startScanning()
                } else if (leftAddress != null && leftAddress.equals(rightAddress, ignoreCase = true)) {
                    // Contaminated storage: both slots hold the identical address.
                    // Connect without forcing a side so GATT service discovery resolves the true hardware side.
                    Log.w(TAG, "Both slots hold the same address ($leftAddress); connecting to determine true hardware side")
                    bleManager.connect(leftAddress, isLeft = null)
                } else {
                    leftAddress?.let {
                        Log.d(TAG, "Reconnecting to saved LEFT insole: $it")
                        bleManager.connect(it, isLeft = true)
                    }
                    rightAddress?.let {
                        Log.d(TAG, "Reconnecting to saved RIGHT insole: $it")
                        bleManager.connect(it, isLeft = false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing BLE connection", e)
            }
        }
    }

    // ── automatic reconnection ──────────────────────────────────────────────

    private fun startReconnectWatchers() {
        if (watchersStarted) return
        watchersStarted = true
        scope.launch { watchSideForReconnect(isLeft = true) }
        scope.launch { watchSideForReconnect(isLeft = false) }
    }

    private suspend fun watchSideForReconnect(isLeft: Boolean) {
        val stateFlow = if (isLeft) bleManager.leftDeviceState else bleManager.rightDeviceState
        stateFlow.collect { state ->
            when (state) {
                is BleDeviceState.Connected -> {
                    policyFor(isLeft).onConnected()
                    // Persist on every successful connection — the old
                    // saveDeviceConnection API had no callers, so startup
                    // reconnect never actually had an address to restore.
                    bleManager.connectedAddressFor(isLeft)?.let { address ->
                        saveDeviceConnection(address, deviceName = "", isLeft = isLeft)
                    }
                }
                is BleDeviceState.Disconnected -> maybeScheduleRetry(isLeft)
                else -> { /* Connecting/Discovering/Error: nothing to do */ }
            }
        }
    }

    private fun maybeScheduleRetry(isLeft: Boolean) {
        if (bleManager.isUserInitiatedDisconnect || bleManager.isDebugBridgeActive) return
        val address = getSavedDeviceAddress(isLeft) ?: return
        if (retryInFlight(isLeft)) return
        setRetryInFlight(isLeft, true)

        scope.launch {
            try {
                val policy = policyFor(isLeft)
                if (!policy.shouldRetry()) {
                    Log.w(TAG, "Reconnect budget exhausted for ${if (isLeft) "LEFT" else "RIGHT"} ($address)")
                    return@launch
                }
                policy.onRetryScheduled()
                delay(policy.retryIntervalMs)

                // Re-validate after the wait: the state may have moved on,
                // the user may have ended the session, Bluetooth may be off.
                val current = if (isLeft) bleManager.leftDeviceState.value else bleManager.rightDeviceState.value
                val stillBusy = current is BleDeviceState.Connecting ||
                    current is BleDeviceState.Discovering ||
                    current is BleDeviceState.Connected
                if (stillBusy || bleManager.isUserInitiatedDisconnect || bleManager.isDebugBridgeActive || !bleManager.isBluetoothEnabled()) {
                    return@launch
                }

                Log.d(TAG, "Reconnect attempt ${policy.attempts}/${ReconnectPolicy.MAX_RECONNECTION_ATTEMPTS} for ${if (isLeft) "LEFT" else "RIGHT"}")
                bleManager.connect(address, isLeft = isLeft)
            } finally {
                setRetryInFlight(isLeft, false)
            }
        }
    }

    private fun policyFor(isLeft: Boolean) = if (isLeft) leftRetryPolicy else rightRetryPolicy

    private fun retryInFlight(isLeft: Boolean) = if (isLeft) leftRetryInFlight else rightRetryInFlight

    private fun setRetryInFlight(isLeft: Boolean, value: Boolean) {
        if (isLeft) leftRetryInFlight = value else rightRetryInFlight = value
    }

    // ── persistence ─────────────────────────────────────────────────────────

    private fun prefs() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun migrateLegacySingleAddress() {
        val legacy = prefs().getString(LEGACY_SAVED_DEVICE_ADDRESS, null) ?: return
        prefs().edit()
            .putString(SAVED_LEFT_ADDRESS, legacy)
            .remove(LEGACY_SAVED_DEVICE_ADDRESS)
            .apply()
        Log.d(TAG, "Migrated legacy saved address to the LEFT slot: $legacy")
    }

    /**
     * Save device connection for automatic reconnection, per foot.
     */
    fun saveDeviceConnection(deviceAddress: String, deviceName: String, isLeft: Boolean) {
        prefs().edit().apply {
            if (isLeft) {
                putString(SAVED_LEFT_ADDRESS, deviceAddress)
                putString(SAVED_LEFT_NAME, deviceName)
                // If the other slot currently holds the exact same address, clear it
                if (prefs().getString(SAVED_RIGHT_ADDRESS, null)?.equals(deviceAddress, ignoreCase = true) == true) {
                    remove(SAVED_RIGHT_ADDRESS)
                    remove(SAVED_RIGHT_NAME)
                }
            } else {
                putString(SAVED_RIGHT_ADDRESS, deviceAddress)
                putString(SAVED_RIGHT_NAME, deviceName)
                // If the other slot currently holds the exact same address, clear it
                if (prefs().getString(SAVED_LEFT_ADDRESS, null)?.equals(deviceAddress, ignoreCase = true) == true) {
                    remove(SAVED_LEFT_ADDRESS)
                    remove(SAVED_LEFT_NAME)
                }
            }
            apply()
        }
        Log.d(TAG, "Saved ${if (isLeft) "LEFT" else "RIGHT"} device connection: $deviceAddress")
    }

    /** Get previously saved device address for one foot. */
    fun getSavedDeviceAddress(isLeft: Boolean): String? =
        prefs().getString(if (isLeft) SAVED_LEFT_ADDRESS else SAVED_RIGHT_ADDRESS, null)

    /** Clear both saved devices. */
    fun clearSavedDevice() {
        prefs().edit().clear().apply()
        leftRetryPolicy.reset()
        rightRetryPolicy.reset()
        Log.d(TAG, "Cleared saved device connections")
    }

    /** Disconnect from device. */
    fun disconnectDevice() {
        scope.launch {
            bleManager.disconnect()
            Log.d(TAG, "Disconnected from device")
        }
    }
}
