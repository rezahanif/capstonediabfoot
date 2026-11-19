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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

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

    // True after an app-driven disconnect() ("End Session") — automatic
    // reconnect watchers must not fight the user. Cleared by connect() and
    // on the next successful service discovery.
    @Volatile
    var isUserInitiatedDisconnect = false
        private set

    @Volatile
    var isDebugBridgeActive = false
        private set

    /** The address currently registered for a side, if any (for reconnect bookkeeping). */
    fun connectedAddressFor(isLeft: Boolean): String? =
        addressToSide.entries.firstOrNull { it.value == isLeft }?.key

    private val _scannedDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val scannedDevices: StateFlow<List<ScannedDevice>> = _scannedDevices

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    // ✅ Multicast flow for incoming data - avoids listener conflicts
    private val _telemetryFlow = MutableSharedFlow<BleDataPacket>(extraBufferCapacity = 64)
    val telemetryFlow = _telemetryFlow.asSharedFlow()

    // Observability for silent drops: tryEmit discards when the 64-slot buffer
    // is full. Emission happens on a binder thread, so we must not suspend —
    // we count and warn instead.
    private val droppedPacketCount = AtomicLong(0)
    private val lastDropWarnElapsedMs = AtomicLong(0L)

    // ── CCCD write serialization (L8a) ──────────────────────────────────────
    // Android allows only one GATT write in flight; firing every descriptor
    // write back-to-back gets later CCCD writes silently dropped on some
    // stacks, leaving characteristics unsubscribed (no data at all). Writes
    // are queued here and released one-by-one from onDescriptorWrite.
    private val descriptorWriteLock = Any()
    private val pendingNotifyChars = ArrayDeque<Pair<BluetoothGatt, BluetoothGattCharacteristic>>()
    private var descriptorWriteInFlight = false

    // Map to track multiple active GATT connections: address -> BluetoothGatt
    // ConcurrentHashMap: mutated from both the main thread (connect/disconnect)
    // and GATT binder threads (callbacks) — a plain HashMap can corrupt here.
    private val activeGatts = ConcurrentHashMap<String, BluetoothGatt>()

    // ✅ Lock this address to its side permanently at connection time
    private val addressToSide = ConcurrentHashMap<String, Boolean>() // address -> isLeft

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                // 1. Identify side by Service UUID first (UUID is guaranteed in primary advertisement packet)
                val serviceUuid = it.scanRecord?.serviceUuids?.firstOrNull { parcelUuid ->
                    InsoleUUIDs.identifySide(parcelUuid.toString()) != "UNKNOWN"
                }?.toString() ?: it.scanRecord?.serviceUuids?.firstOrNull()?.toString()
                val sideByUuid = InsoleUUIDs.identifySide(serviceUuid)

                // 2. Extract or infer device name (fallback to canonical insole name if name is deferred to scan response)
                var deviceName = it.scanRecord?.deviceName ?: it.device.name ?: "Unknown"
                if (deviceName == "Unknown") {
                    if (sideByUuid == "LEFT") deviceName = "Smart_Insole_Left"
                    else if (sideByUuid == "RIGHT") deviceName = "Smart_Insole_Right"
                }

                // 3. Filter: Keep only devices that match recognized insole UUIDs or contain "Insole" in name
                val isRecognizedInsole = sideByUuid != "UNKNOWN" ||
                        (deviceName != "Unknown" && deviceName.contains("Insole", ignoreCase = true))
                if (!isRecognizedInsole) return@let

                Log.d("InsoleBleManager", "Discovered insole: $deviceName [${it.device.address}] UUID=$serviceUuid RSSI=${it.rssi}")

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

    /**
     * Internal helper to initiate bonding.
     * Currently not called in the connection flow to maintain "Just Works" compatibility.
     */
    private fun bondDevice(device: BluetoothDevice) {
        if (device.bondState == BluetoothDevice.BOND_NONE) {
            Log.i("InsoleBleManager", "Initiating bond with ${device.address}")
            device.createBond()
        }
    }

    init {
        _isBluetoothEnabled.value = isBluetoothEnabled()
        startDebugBridgeServer()
    }

    fun injectDebugTelemetry(line: String, isLeft: Boolean, address: String) {
        if (isLeft) {
            _leftDeviceState.value = BleDeviceState.Connected
            addressToSide[address] = true
        } else {
            _rightDeviceState.value = BleDeviceState.Connected
            addressToSide[address] = false
        }
        _telemetryFlow.tryEmit(BleDataPacket(line.toByteArray(Charsets.UTF_8), isLeft, address))
    }

    private fun startDebugBridgeServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val server = java.net.ServerSocket(9999)
                Log.i("InsoleBleManager", "Debug telemetry server listening on port 9999")
                while (isActive) {
                    val socket = server.accept()
                    Log.i("InsoleBleManager", "Debug telemetry client connected from ${socket.remoteSocketAddress}")
                    isDebugBridgeActive = true
                    _leftDeviceState.value = BleDeviceState.Connected
                    _rightDeviceState.value = BleDeviceState.Connected
                    addressToSide["10:00:3B:D1:E0:B2"] = true
                    addressToSide["10:00:3B:D1:E0:B3"] = false
                    _scannedDevices.value = listOf(
                        ScannedDevice(name = "Smart_Insole_Left", address = "10:00:3B:D1:E0:B2", rssi = -58, serviceUuid = InsoleUUIDs.LEFT_SERVICE),
                        ScannedDevice(name = "Smart_Insole_Right", address = "10:00:3B:D1:E0:B3", rssi = -61, serviceUuid = InsoleUUIDs.RIGHT_SERVICE)
                    )

                    try {
                        val reader = java.io.BufferedReader(java.io.InputStreamReader(socket.getInputStream()))
                        while (isActive) {
                            val line = reader.readLine() ?: break
                            val trimmed = line.trim()
                            if (trimmed.isEmpty()) continue
                            if (trimmed.equals("DISCONNECT", ignoreCase = true)) {
                                _leftDeviceState.value = BleDeviceState.Disconnected
                                _rightDeviceState.value = BleDeviceState.Disconnected
                                break
                            }
                            val parts = trimmed.split(":", limit = 2)
                            if (parts.size == 2) {
                                val sideStr = parts[0].trim().uppercase()
                                val payload = parts[1].trim()
                                val isLeft = sideStr == "LEFT"
                                val addr = if (isLeft) "10:00:3B:D1:E0:B2" else "10:00:3B:D1:E0:B3"
                                injectDebugTelemetry(payload, isLeft, addr)
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("InsoleBleManager", "Debug telemetry client disconnected: ${e.message}")
                    } finally {
                        isDebugBridgeActive = false
                        try { socket.close() } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                Log.w("InsoleBleManager", "Debug bridge server stopped: ${e.message}")
            }
        }
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

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        Log.d("InsoleBleManager", "Starting BLE scan (unfiltered for broad chipset compatibility)...")
        scanner.startScan(emptyList(), settings, scanCallback)
    }

    fun stopScanning() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        _isScanning.value = false
    }

    /**
     * Connects to a device and attempts to identify if it is Left or Right.
     */
    fun connect(deviceAddress: String, isLeft: Boolean? = null) {
        if (isDebugBridgeActive) {
            Log.d("InsoleBleManager", "connect() ignored: debug USB bridge is active")
            return
        }
        if (!isBluetoothEnabled()) return

        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress) ?: return

        isUserInitiatedDisconnect = false

        // Use provided side if available, otherwise determine from UUID/Name
        val determinedIsLeft = isLeft ?: run {
            val scannedDevice = _scannedDevices.value.find { it.address.equals(deviceAddress, ignoreCase = true) }
            val side = InsoleUUIDs.identifySide(scannedDevice?.serviceUuid, scannedDevice?.name ?: device.name)
            when (side) {
                "LEFT" -> true
                "RIGHT" -> false
                else -> {
                    val nameToCheck = scannedDevice?.name ?: device.name
                    if (nameToCheck?.contains("Right", ignoreCase = true) == true) false
                    else if (nameToCheck?.contains("Left", ignoreCase = true) == true) true
                    else true
                }
            }
        }

        Log.d("InsoleBleManager", "Connecting to ${device.name ?: "SM"} ($deviceAddress) identified as ${if(determinedIsLeft) "LEFT" else "RIGHT"}")

        // ✅ Lock this address to its side permanently
        addressToSide[deviceAddress] = determinedIsLeft

        if (determinedIsLeft) _leftDeviceState.value = BleDeviceState.Connecting
        else _rightDeviceState.value = BleDeviceState.Connecting

        // A second connect() for the same address would orphan the first
        // GATT (overwritten without close) — release the stale one first.
        activeGatts.remove(deviceAddress)?.let { stale ->
            Log.w("InsoleBleManager", "Replacing an existing GATT for $deviceAddress")
            stale.disconnect()
            stale.close()
        }

        val gatt = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            device.connectGatt(context, false, this, BluetoothDevice.TRANSPORT_LE)
        } else {
            @Suppress("DEPRECATION")
            device.connectGatt(context, false, this)
        }
        activeGatts[deviceAddress] = gatt
    }

    fun disconnect(deviceAddress: String? = null) {
        isUserInitiatedDisconnect = true
        if (deviceAddress != null) {
            activeGatts[deviceAddress]?.let { gatt ->
                val isLeft = addressToSide[deviceAddress] ?: true
                updateDeviceState(isLeft, BleDeviceState.Disconnected)
                gatt.disconnect()
                // Do not close immediately; let onConnectionStateChange handle it
                // Or if we must close, do it after a slight delay or just disconnect.
            }
        } else {
            // Disconnect all
            activeGatts.forEach { (address, gatt) ->
                val isLeft = addressToSide[address] ?: true
                updateDeviceState(isLeft, BleDeviceState.Disconnected)
                gatt.disconnect()
            }
        }
    }

    // Helper function to extract side directly from active device records
    private fun determineSideFromGatt(gatt: BluetoothGatt): Boolean {
        // 1. Primary check: Use verified Service UUIDs if available
        val services = gatt.services
        if (!services.isNullOrEmpty()) {
            for (service in services) {
                val uuidStr = service.uuid.toString()
                if (uuidStr.equals(InsoleUUIDs.LEFT_SERVICE, ignoreCase = true)) return true
                if (uuidStr.equals(InsoleUUIDs.RIGHT_SERVICE, ignoreCase = true)) return false
            }
        }

        // 2. Secondary check: Scanned device cache (has accurate advertised name and service UUID)
        val scanned = _scannedDevices.value.find { it.address.equals(gatt.device.address, ignoreCase = true) }
        val scannedSide = InsoleUUIDs.identifySide(scanned?.serviceUuid, scanned?.name)
        if (scannedSide == "LEFT") return true
        if (scannedSide == "RIGHT") return false

        // 3. Fallback check: Read direct name profile strings if services aren't fully indexed yet
        val devName = gatt.device.name
        if (devName != null) {
            if (devName.contains("Right", ignoreCase = true)) return false
            if (devName.contains("Left", ignoreCase = true)) return true
        }

        // 4. Default: assume left only as absolute last resort
        return true
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        if (gatt == null) return

        val address = gatt.device.address
        // Re-lock the side if it was cleared by a prior disconnect; persist so
        // handleCharacteristicChange can find it without another lookup.
        val isLeft = addressToSide.getOrPut(address) { determineSideFromGatt(gatt) }

        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                updateDeviceState(isLeft, BleDeviceState.Discovering)
                // ✅ Request higher MTU for long CSV strings before discovering services
                if (!gatt.requestMtu(512)) {
                    Log.w("InsoleBleManager", "MTU request could not be initiated, discovering services...")
                    gatt.discoverServices()
                }
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                // Ensure proper state cleanup on target channel path
                updateDeviceState(isLeft, BleDeviceState.Disconnected)
                // Drop queued CCCD writes for this device and release the
                // in-flight slot — a write to a closed gatt may never
                // callback and would otherwise wedge the other insole's queue.
                synchronized(descriptorWriteLock) {
                    pendingNotifyChars.removeAll { it.first === gatt }
                    descriptorWriteInFlight = false
                }
                gatt.close()
                // Only clean the maps if this is the CURRENT connection for
                // the address — a stale replaced GATT's late DISCONNECTED
                // callback must not unregister the fresh one.
                if (activeGatts[address] === gatt) {
                    activeGatts.remove(address)
                    addressToSide.remove(address)
                }
            }
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
            Log.d("InsoleBleManager", "MTU changed to $mtu for ${gatt.device.address}")
            // Now safe to discover services
            gatt.discoverServices()
        } else if (gatt != null) {
            Log.e("InsoleBleManager", "MTU request failed, falling back to discovery anyway")
            gatt.discoverServices()
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (gatt == null) return

        val address = gatt.device.address
        // Re-evaluate side from the authoritative discovered GATT services
        val verifiedSide = determineSideFromGatt(gatt)
        val oldSide = addressToSide[address]
        if (oldSide != null && oldSide != verifiedSide) {
            Log.w("InsoleBleManager", "Correcting side for $address from ${if (oldSide) "LEFT" else "RIGHT"} to ${if (verifiedSide) "LEFT" else "RIGHT"} after service discovery")
            updateDeviceState(oldSide, BleDeviceState.Disconnected)
        }
        addressToSide[address] = verifiedSide
        val isLeft = verifiedSide

        if (status == BluetoothGatt.GATT_SUCCESS) {
            queueNotifications(gatt)
            updateDeviceState(isLeft, BleDeviceState.Connected)
        } else {
            updateDeviceState(isLeft, BleDeviceState.Error("Service discovery failed"))
        }
    }

    private fun updateDeviceState(isLeft: Boolean, state: BleDeviceState) {
        if (isLeft) _leftDeviceState.value = state
        else _rightDeviceState.value = state
    }

    /**
     * Queues every notify-capable characteristic for CCCD subscription and
     * starts draining the queue. Kept subscribe-all (not filtered by the
     * InsoleUUIDs characteristic constants) because the firmware's actual
     * notify characteristic was never confirmed — see TECH_DEBT.md.
     */
    private fun queueNotifications(gatt: BluetoothGatt) {
        synchronized(descriptorWriteLock) {
            gatt.services.forEach { service ->
                service.characteristics.forEach { characteristic ->
                    if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                        pendingNotifyChars.addLast(gatt to characteristic)
                    }
                }
            }
        }
        writeNextDescriptor(gatt)
    }

    /** Drains the queue while no descriptor write is in flight. */
    private fun writeNextDescriptor(gatt: BluetoothGatt) {
        synchronized(descriptorWriteLock) {
            while (!descriptorWriteInFlight) {
                val next = pendingNotifyChars.removeFirstOrNull() ?: return
                descriptorWriteInFlight = true
                if (!enableNotifications(next.first, next.second)) {
                    Log.w("InsoleBleManager", "CCCD write failed for ${next.second.uuid}; continuing queue")
                    descriptorWriteInFlight = false
                }
            }
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
        if (gatt == null) return
        // Advance the queue regardless of status — a failed CCCD write must
        // not wedge the remaining subscriptions.
        synchronized(descriptorWriteLock) { descriptorWriteInFlight = false }
        writeNextDescriptor(gatt)
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic): Boolean {
        Log.d("InsoleBleManager", "Enabling notifications for ${characteristic.uuid}")
        if (!gatt.setCharacteristicNotification(characteristic, true)) return false

        val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
            ?: return false
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) ==
                android.bluetooth.BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(descriptor)
        }
    }

    // ── REQUIRED for API 33+ (use `value` param) ─────────────────────────────
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        handleCharacteristicChange(gatt, characteristic, value)
    }

    // ── REQUIRED for API < 33 (use `characteristic.value`) ──────────────────
    @Deprecated("Deprecated in API 33")
    @Suppress("DEPRECATION")
    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return // Handled by 3-arg overload on API 33+ to prevent duplicate dispatch
        }
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
        // CRITICAL FIX: was `?: return` — silently dropped ALL data after any disconnect/reconnect
        // because addressToSide.remove() is called on disconnect but was never re-persisted on
        // reconnect. Now we fall back to service-UUID detection AND persist the result so
        // subsequent calls don't repeat the lookup.
        val isLeft = addressToSide.getOrPut(address) { determineSideFromGatt(gatt) }

        // Broadcast via Flow instead of a single listener variable
        val rxTimestampNs = System.nanoTime()
        val emitted = _telemetryFlow.tryEmit(BleDataPacket(value, isLeft, address))
        if (!emitted) {
            val drops = droppedPacketCount.incrementAndGet()
            Log.w("DIABFOOT_TEST", "BUFFER_DROP addr=$address drops=$drops")
            val nowMs = android.os.SystemClock.elapsedRealtime()
            val lastWarn = lastDropWarnElapsedMs.get()
            if (nowMs - lastWarn > 5000L && lastDropWarnElapsedMs.compareAndSet(lastWarn, nowMs)) {
                Log.w("InsoleBleManager", "Telemetry buffer overflow: $drops packets dropped so far (consumer too slow)")
            }
        } else {
            Log.i("DIABFOOT_TEST", "GATT_NOTIFY addr=$address side=${if(isLeft) "LEFT" else "RIGHT"} bytes=${value.size} ts_ns=$rxTimestampNs")
        }
    }

    // Deprecated but maintained for multi-listener transition
    fun setOnCharacteristicChangedListener(listener: (ByteArray, Boolean, String) -> Unit) {
        // No longer used, but kept to avoid compile errors until callers migrate
    }
}