package com.project.insole.core.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.project.insole.core.ble.model.BleDeviceState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

data class BleDataPacket(
    val data: ByteArray,
    val isLeft: Boolean,
    val deviceAddress: String
)

data class ScannedDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val serviceUuid: String? = null
)

/**
 * Native BLE Manager handling GATT operations, scanning, and MTU configuration.
 * Enhanced to support two concurrent connections (Left & Right insoles).
 */
@SuppressLint("MissingPermission")
class InsoleBleManager(private val context: Context) : BluetoothGattCallback() {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    
    // Independent states for Left and Right insoles
    private val _leftDeviceState = MutableStateFlow<BleDeviceState>(BleDeviceState.Disconnected)
    val leftDeviceState: StateFlow<BleDeviceState> = _leftDeviceState

    private val _rightDeviceState = MutableStateFlow<BleDeviceState>(BleDeviceState.Disconnected)
    val rightDeviceState: StateFlow<BleDeviceState> = _rightDeviceState

    private val _scannedDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val scannedDevices: StateFlow<List<ScannedDevice>> = _scannedDevices

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning
    
    // ✅ Multicast flow for incoming data - avoids listener conflicts
    private val _telemetryFlow = MutableSharedFlow<BleDataPacket>(extraBufferCapacity = 64)
    val telemetryFlow = _telemetryFlow.asSharedFlow()

    // Map to track multiple active GATT connections: address -> BluetoothGatt
    private val activeGatts = mutableMapOf<String, BluetoothGatt>()

    // ✅ Lock this address to its side permanently at connection time
    private val addressToSide = mutableMapOf<String, Boolean>() // address -> isLeft

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                val deviceName = it.device.name ?: "Unknown"
                if (deviceName == "Unknown") return@let

                // Extract Service UUID if available
                val serviceUuid = it.scanRecord?.serviceUuids?.firstOrNull()?.toString()

                val device = ScannedDevice(
                    name = deviceName,
                    address = it.device.address,
                    rssi = it.rssi,
                    serviceUuid = serviceUuid
                )
                
                val currentList = _scannedDevices.value.toMutableList()
                currentList.removeAll { existing -> existing.address == device.address }
                currentList.add(device)
                
                // Sort by:
                // 1. Recognized Insole UUIDs first
                // 2. Known "Smart_Insole" names second
                // 3. RSSI (signal strength) third
                val sortedList = currentList.sortedWith(
                    compareByDescending<ScannedDevice> { 
                        InsoleUUIDs.identifySide(it.serviceUuid) != "UNKNOWN"
                    }.thenByDescending { 
                        it.name.contains("Smart_Insole", ignoreCase = true) 
                    }.thenByDescending { it.rssi }
                )
                
                _scannedDevices.value = sortedList
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            _isScanning.value = false
        }
    }

    private val _isBluetoothEnabled = MutableStateFlow(false)
    val isBluetoothEnabledFlow: StateFlow<Boolean> = _isBluetoothEnabled

    init {
        _isBluetoothEnabled.value = isBluetoothEnabled()
    }

    fun isBluetoothEnabled(): Boolean {
        val enabled = bluetoothAdapter?.isEnabled == true
        _isBluetoothEnabled.value = enabled
        return enabled
    }

    fun startScanning() {
        if (!isBluetoothEnabled()) return
        
        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return
        if (_isScanning.value) return

        _isScanning.value = true
        _scannedDevices.value = emptyList()

        // Filters for both Left and Right Service UUIDs
        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(InsoleUUIDs.LEFT_SERVICE)).build(),
            ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(InsoleUUIDs.RIGHT_SERVICE)).build()
        )

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        scanner.startScan(filters, settings, scanCallback)
    }

    fun stopScanning() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        _isScanning.value = false
    }

    /**
     * Connects to a device and attempts to identify if it is Left or Right.
     */
    fun connect(deviceAddress: String, isLeft: Boolean? = null) {
        if (!isBluetoothEnabled()) return
        
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress) ?: return
        
        // Use provided side if available, otherwise determine from UUID/Name
        val determinedIsLeft = isLeft ?: run {
            val scannedDevice = _scannedDevices.value.find { it.address == deviceAddress }
            val uuidSide = InsoleUUIDs.identifySide(scannedDevice?.serviceUuid)
            if (uuidSide != "UNKNOWN") uuidSide == "LEFT"
            else device.name?.contains("Left", ignoreCase = true) ?: true
        }

        Log.d("InsoleBleManager", "Connecting to ${device.name ?: "SM"} ($deviceAddress) identified as ${if(determinedIsLeft) "LEFT" else "RIGHT"}")

        // ✅ Lock this address to its side permanently
        addressToSide[deviceAddress] = determinedIsLeft

        if (determinedIsLeft) _leftDeviceState.value = BleDeviceState.Connecting
        else _rightDeviceState.value = BleDeviceState.Connecting

        val gatt = device.connectGatt(context, false, this)
        activeGatts[deviceAddress] = gatt
    }

    fun disconnect(deviceAddress: String? = null) {
        if (deviceAddress != null) {
            activeGatts[deviceAddress]?.let { gatt ->
                gatt.disconnect()
                gatt.close()
                activeGatts.remove(deviceAddress)
                addressToSide.remove(deviceAddress)
            }
        } else {
            // Disconnect all
            activeGatts.values.forEach { 
                it.disconnect()
                it.close()
            }
            activeGatts.clear()
            addressToSide.clear()
        }
    }

    // Helper function to extract side directly from active device records
    private fun determineSideFromGatt(gatt: BluetoothGatt): Boolean {
        // 1. Primary check: Use verified Service UUIDs if available
        val services = gatt.services
        if (services.isNotEmpty()) {
            for (service in services) {
                val uuidStr = service.uuid.toString()
                if (uuidStr.equals(InsoleUUIDs.LEFT_SERVICE, ignoreCase = true)) return true
                if (uuidStr.equals(InsoleUUIDs.RIGHT_SERVICE, ignoreCase = true)) return false
            }
        }
        
        // 2. Fallback check: Read direct name profile strings if services aren't fully indexed yet
        return gatt.device.name?.contains("Left", ignoreCase = true) ?: true
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        if (gatt == null) return
        
        val address = gatt.device.address
        // ✅ Use the pre-locked side, fallback to dynamic check only if not found
        val isLeft = addressToSide[address] ?: determineSideFromGatt(gatt)

        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                updateDeviceState(isLeft, BleDeviceState.Discovering)
                gatt.discoverServices()
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                // Ensure proper state cleanup on target channel path
                updateDeviceState(isLeft, BleDeviceState.Disconnected)
                gatt.close()
                activeGatts.remove(address)
                addressToSide.remove(address)
            }
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (gatt == null) return
        
        val address = gatt.device.address
        // ✅ Reliable — uses the address-locked side
        val isLeft = addressToSide[address] ?: determineSideFromGatt(gatt)

        if (status == BluetoothGatt.GATT_SUCCESS) {
            gatt.services.forEach { service ->
                service.characteristics.forEach { characteristic ->
                    if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                        enableNotifications(gatt, characteristic)
                    }
                }
            }
            updateDeviceState(isLeft, BleDeviceState.Connected)
        } else {
            updateDeviceState(isLeft, BleDeviceState.Error("Service discovery failed"))
        }
    }

    private fun updateDeviceState(isLeft: Boolean, state: BleDeviceState) {
        if (isLeft) _leftDeviceState.value = state
        else _rightDeviceState.value = state
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        Log.d("InsoleBleManager", "Enabling notifications for ${characteristic.uuid}")
        gatt.setCharacteristicNotification(characteristic, true)
        
        val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
        if (descriptor != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor)
            }
        }
    }

    // ── REQUIRED for API 33+ (use `value` param) ─────────────────────────────
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        super.onCharacteristicChanged(gatt, characteristic, value)
        handleCharacteristicChange(gatt, characteristic, value)
    }

    // ── REQUIRED for API < 33 (use `characteristic.value`) ──────────────────
    @Deprecated("Deprecated in API 33")
    @Suppress("DEPRECATION")
    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        super.onCharacteristicChanged(gatt, characteristic)
        if (gatt == null || characteristic == null) return
        val data = characteristic.value ?: return
        handleCharacteristicChange(gatt, characteristic, data)
    }

    private fun handleCharacteristicChange(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val address = gatt.device.address
        val isLeft = addressToSide[address] ?: return
        
        // Broadcast via Flow instead of a single listener variable
        _telemetryFlow.tryEmit(BleDataPacket(value, isLeft, address))
    }

    // Deprecated but maintained for multi-listener transition
    fun setOnCharacteristicChangedListener(listener: (ByteArray, Boolean, String) -> Unit) {
        // No longer used, but kept to avoid compile errors until callers migrate
    }
}
