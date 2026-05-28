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
import android.bluetooth.le.ScanResult
import android.content.Context
import com.project.insole.core.ble.model.BleDeviceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ScannedDevice(
    val name: String,
    val address: String,
    val rssi: Int
)

/**
 * Native BLE Manager handling GATT operations, scanning, and MTU configuration.
 * This is the single entry point for all BLE communication with the ESP32 insole.
 */
@SuppressLint("MissingPermission")
class InsoleBleManager(context: Context) : BluetoothGattCallback() {

    private val context = context
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    
    private val _bleDeviceState = MutableStateFlow<BleDeviceState>(BleDeviceState.Disconnected)
    val bleDeviceState: StateFlow<BleDeviceState> = _bleDeviceState

    private val _scannedDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val scannedDevices: StateFlow<List<ScannedDevice>> = _scannedDevices

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning
    
    private var gatt: BluetoothGatt? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                val device = ScannedDevice(
                    name = it.device.name ?: "Unknown",
                    address = it.device.address,
                    rssi = it.rssi
                )
                
                // Add to list if not already present
                val currentList = _scannedDevices.value.toMutableList()
                currentList.removeAll { existing -> existing.address == device.address }
                currentList.add(device)
                _scannedDevices.value = currentList
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            _isScanning.value = false
        }
    }

    /**
     * Check if Bluetooth is enabled.
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * Start scanning for BLE devices.
     */
    fun startScanning() {
        if (!isBluetoothEnabled()) return

        _isScanning.value = true
        _scannedDevices.value = emptyList()
        
        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
    }

    /**
     * Stop scanning for BLE devices.
     */
    fun stopScanning() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        _isScanning.value = false
    }

    /**
     * Connects to a BLE device by address.
     */
    fun connect(deviceAddress: String) {
        if (!isBluetoothEnabled()) return
        
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        _bleDeviceState.value = BleDeviceState.Connecting
        
        gatt = device?.connectGatt(context, false, this)
    }

    /**
     * Disconnects from the current device.
     */
    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _bleDeviceState.value = BleDeviceState.Disconnected
    }

    /**
     * Requests MTU size update for faster data transfer.
     * minSdk = 24, so LOLLIPOP (API 21) check is always true and omitted.
     */
    fun requestMtu(mtuSize: Int) {
        gatt?.requestMtu(mtuSize)
    }

    /**
     * Reads characteristic value from the device.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        gatt?.readCharacteristic(characteristic)
    }

    /**
     * Writes data to a characteristic on the device.
     */
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, data: ByteArray) {
        characteristic.value = data
        gatt?.writeCharacteristic(characteristic)
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                this.gatt = gatt
                _bleDeviceState.value = BleDeviceState.Discovering
                // Discover services after connection
                gatt?.discoverServices()
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                _bleDeviceState.value = BleDeviceState.Disconnected
                this.gatt?.close()
                this.gatt = null
            }
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        
        if (status == BluetoothGatt.GATT_SUCCESS) {
            _bleDeviceState.value = BleDeviceState.Connected
            // Request MTU for better performance
            requestMtu(512)
        } else {
            _bleDeviceState.value = BleDeviceState.Error("Service discovery failed")
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
            // Data received - handle in BleSensorDataSource
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        
        if (status == BluetoothGatt.GATT_SUCCESS) {
            // Write successful
        } else {
            _bleDeviceState.value = BleDeviceState.Error("Write failed")
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        
        characteristic?.value?.let {
            // Raw bytes received - pass to BleSensorDataSource for parsing
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
        
        if (status == BluetoothGatt.GATT_SUCCESS) {
            // Notification enabled
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        
        if (status == BluetoothGatt.GATT_SUCCESS) {
            // MTU updated successfully
        }
    }
}
