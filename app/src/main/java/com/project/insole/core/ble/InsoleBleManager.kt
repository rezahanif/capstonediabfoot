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
                val deviceName = it.device.name ?: "Unknown"
                
                // 1. Filter: Skip "Unknown" devices
                if (deviceName == "Unknown") return@let

                Log.d("InsoleBleManager", "Found device: $deviceName (${it.device.address})")
                
                val device = ScannedDevice(
                    name = deviceName,
                    address = it.device.address,
                    rssi = it.rssi
                )
                
                // 2. Add and Sort: Prioritize "insole" at the top
                val currentList = _scannedDevices.value.toMutableList()
                currentList.removeAll { existing -> existing.address == device.address }
                currentList.add(device)
                
                // Sort logic: "insole" first (case-insensitive), then others alphabetically
                val sortedList = currentList.sortedWith(compareByDescending<ScannedDevice> { 
                    it.name.contains("insole", ignoreCase = true) 
                }.thenBy { it.name })
                
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

    /**
     * Check if Bluetooth is enabled.
     */
    fun isBluetoothEnabled(): Boolean {
        val enabled = bluetoothAdapter?.isEnabled == true
        _isBluetoothEnabled.value = enabled
        return enabled
    }

    /**
     * Start scanning for BLE devices with dual insole filters.
     */
    fun startScanning() {
        if (!isBluetoothEnabled()) {
            Log.w("InsoleBleManager", "Bluetooth is disabled, cannot scan")
            return
        }
        
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            Log.e("InsoleBleManager", "BluetoothLeScanner is null. Is Bluetooth ON?")
            _bleDeviceState.value = BleDeviceState.Error("Scanner not available")
            return
        }

        if (_isScanning.value) {
            Log.d("InsoleBleManager", "Already scanning, ignoring start request")
            return
        }

        Log.d("InsoleBleManager", "Starting BLE scan for Za and Pek insoles...")
        _isScanning.value = true
        _scannedDevices.value = emptyList()

        // ── Dual Service UUID Filters ──────────────────────────────────────
        val uuidZa = ParcelUuid.fromString("4fa2c732-ca9a-4c20-9492-c167df3c942a")
        val uuidPek = ParcelUuid.fromString("4fa2c732-ca9a-4c20-9492-c167df3c942b")

        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(uuidZa).build(),
            ScanFilter.Builder().setServiceUuid(uuidPek).build()
        )

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        scanner.startScan(filters, settings, scanCallback)
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
        
        if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
            Log.d("InsoleBleManager", "Services discovered. Enabling notifications...")
            
            // Loop through services and characteristics to find the one to notify
            gatt.services.forEach { service ->
                service.characteristics.forEach { characteristic ->
                    val properties = characteristic.properties
                    if (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                        enableNotifications(gatt, characteristic)
                    }
                }
            }
            
            _bleDeviceState.value = BleDeviceState.Connected
            requestMtu(512)
        } else {
            _bleDeviceState.value = BleDeviceState.Error("Service discovery failed")
        }
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        Log.d("InsoleBleManager", "Enabling notifications for ${characteristic.uuid}")
        gatt.setCharacteristicNotification(characteristic, true)
        
        // Write to Descriptor to truly enable notifications on the peripheral
        val descriptor = characteristic.getDescriptor(
            java.util.UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        )
        if (descriptor != null) {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
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
        
        characteristic?.value?.let { bytes ->
            // Notify listeners (BleSensorDataSource)
            characteristicChangedListener?.invoke(bytes)
        }
    }

    private var characteristicChangedListener: ((ByteArray) -> Unit)? = null

    fun setOnCharacteristicChangedListener(listener: (ByteArray) -> Unit) {
        characteristicChangedListener = listener
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
