package com.project.insole.core.ble;

@kotlin.Metadata(mv = {2, 3, 0}, k = 2, xi = 48, d1 = {"\u00008\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\u001a\u001e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a \u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0003\u001a&\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\b2\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u0010\u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u0014H\u0003\u00a8\u0006\u0015"}, d2 = {"BleDevicePairingScreen", "", "viewModel", "Lcom/project/insole/core/ble/BleViewModel;", "onConnected", "Lkotlin/Function0;", "BleStatusCard", "isBluetoothEnabled", "", "connectionState", "Lcom/project/insole/core/ble/model/BleDeviceState;", "connectedDeviceName", "", "DeviceCard", "device", "Lcom/project/insole/core/ble/ScannedDevice;", "isConnected", "onConnect", "Spacer", "modifier", "Landroidx/compose/ui/Modifier;", "app_debug"})
public final class BleDevicePairingScreenKt {
    
    /**
     * BLE Device Pairing Screen - allows user to scan for and connect to insole devices.
     * Shows connection status and list of available devices.
     */
    @androidx.compose.runtime.Composable()
    public static final void BleDevicePairingScreen(@org.jetbrains.annotations.NotNull()
    com.project.insole.core.ble.BleViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onConnected) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void BleStatusCard(boolean isBluetoothEnabled, com.project.insole.core.ble.model.BleDeviceState connectionState, java.lang.String connectedDeviceName) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void DeviceCard(com.project.insole.core.ble.ScannedDevice device, boolean isConnected, kotlin.jvm.functions.Function0<kotlin.Unit> onConnect) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void Spacer(androidx.compose.ui.Modifier modifier) {
    }
}