package com.project.insole.core.ble.model;

/**
 * Represents the connection state of a BLE device.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0005\u0004\u0005\u0006\u0007\bB\t\b\u0004\u00a2\u0006\u0004\b\u0002\u0010\u0003\u0082\u0001\u0005\t\n\u000b\f\r\u00a8\u0006\u000e"}, d2 = {"Lcom/project/insole/core/ble/model/BleDeviceState;", "", "<init>", "()V", "Disconnected", "Connecting", "Connected", "Discovering", "Error", "Lcom/project/insole/core/ble/model/BleDeviceState$Connected;", "Lcom/project/insole/core/ble/model/BleDeviceState$Connecting;", "Lcom/project/insole/core/ble/model/BleDeviceState$Disconnected;", "Lcom/project/insole/core/ble/model/BleDeviceState$Discovering;", "Lcom/project/insole/core/ble/model/BleDeviceState$Error;", "app_debug"})
public abstract class BleDeviceState {
    
    private BleDeviceState() {
        super();
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/project/insole/core/ble/model/BleDeviceState$Connected;", "Lcom/project/insole/core/ble/model/BleDeviceState;", "<init>", "()V", "app_debug"})
    public static final class Connected extends com.project.insole.core.ble.model.BleDeviceState {
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.ble.model.BleDeviceState.Connected INSTANCE = null;
        
        private Connected() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/project/insole/core/ble/model/BleDeviceState$Connecting;", "Lcom/project/insole/core/ble/model/BleDeviceState;", "<init>", "()V", "app_debug"})
    public static final class Connecting extends com.project.insole.core.ble.model.BleDeviceState {
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.ble.model.BleDeviceState.Connecting INSTANCE = null;
        
        private Connecting() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/project/insole/core/ble/model/BleDeviceState$Disconnected;", "Lcom/project/insole/core/ble/model/BleDeviceState;", "<init>", "()V", "app_debug"})
    public static final class Disconnected extends com.project.insole.core.ble.model.BleDeviceState {
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.ble.model.BleDeviceState.Disconnected INSTANCE = null;
        
        private Disconnected() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/project/insole/core/ble/model/BleDeviceState$Discovering;", "Lcom/project/insole/core/ble/model/BleDeviceState;", "<init>", "()V", "app_debug"})
    public static final class Discovering extends com.project.insole.core.ble.model.BleDeviceState {
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.ble.model.BleDeviceState.Discovering INSTANCE = null;
        
        private Discovering() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\t\u0010\b\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\t\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0014\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u00d6\u0083\u0004J\n\u0010\u000e\u001a\u00020\u000fH\u00d6\u0081\u0004J\n\u0010\u0010\u001a\u00020\u0003H\u00d6\u0081\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0011"}, d2 = {"Lcom/project/insole/core/ble/model/BleDeviceState$Error;", "Lcom/project/insole/core/ble/model/BleDeviceState;", "message", "", "<init>", "(Ljava/lang/String;)V", "getMessage", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class Error extends com.project.insole.core.ble.model.BleDeviceState {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String message = null;
        
        public Error(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMessage() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.project.insole.core.ble.model.BleDeviceState.Error copy(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}