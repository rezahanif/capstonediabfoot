package com.project.insole.core.sensor;

/**
 * Central repository for all sensor-related constants.
 * Includes thresholds, sampling rates, and calibration parameters.
 * Update these values based on device specifications and clinical requirements.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u000b\b\u00c7\u0002\u0018\u00002\u00020\u0001:\t\u0003\u0004\u0005\u0006\u0007\b\t\n\u000bB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\f"}, d2 = {"Lcom/project/insole/core/sensor/SensorConstants;", "", "()V", "AlertCooldown", "Calibration", "DataRetention", "DeviceHealth", "PressureThresholds", "SamplingRates", "StepDetection", "TemperatureThresholds", "UnitConversions", "app_debug"})
public final class SensorConstants {
    @org.jetbrains.annotations.NotNull()
    public static final com.project.insole.core.sensor.SensorConstants INSTANCE = null;
    
    private SensorConstants() {
        super();
    }
    
    /**
     * Alert cooldown periods (to avoid alert spam)
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/project/insole/core/sensor/SensorConstants$AlertCooldown;", "", "()V", "BATTERY_ALERT_COOLDOWN", "", "CONNECTION_ALERT_COOLDOWN", "PRESSURE_ALERT_COOLDOWN", "TEMPERATURE_ALERT_COOLDOWN", "app_debug"})
    public static final class AlertCooldown {
        public static final int PRESSURE_ALERT_COOLDOWN = 30;
        public static final int TEMPERATURE_ALERT_COOLDOWN = 60;
        public static final int BATTERY_ALERT_COOLDOWN = 300;
        public static final int CONNECTION_ALERT_COOLDOWN = 60;
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.sensor.SensorConstants.AlertCooldown INSTANCE = null;
        
        private AlertCooldown() {
            super();
        }
    }
    
    /**
     * Calibration parameters
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/project/insole/core/sensor/SensorConstants$Calibration;", "", "()V", "CALIBRATION_MAX_DEVIATION", "", "CALIBRATION_SAMPLES_PER_ZONE", "", "CALIBRATION_TIMEOUT_SECONDS", "SENSOR_GAIN", "ZERO_OFFSET", "app_debug"})
    public static final class Calibration {
        public static final int CALIBRATION_SAMPLES_PER_ZONE = 100;
        public static final float CALIBRATION_MAX_DEVIATION = 15.0F;
        public static final int CALIBRATION_TIMEOUT_SECONDS = 60;
        public static final int ZERO_OFFSET = 5;
        public static final float SENSOR_GAIN = 2.5F;
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.sensor.SensorConstants.Calibration INSTANCE = null;
        
        private Calibration() {
            super();
        }
    }
    
    /**
     * Data retention and storage
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/project/insole/core/sensor/SensorConstants$DataRetention;", "", "()V", "CLOUD_SYNC_INTERVAL_SECONDS", "", "CLOUD_UPLOAD_BATCH_SIZE", "LOCAL_DATA_RETENTION_HOURS", "MAX_DAILY_ENTRIES", "app_debug"})
    public static final class DataRetention {
        public static final int LOCAL_DATA_RETENTION_HOURS = 24;
        public static final int MAX_DAILY_ENTRIES = 1440;
        public static final int CLOUD_SYNC_INTERVAL_SECONDS = 300;
        public static final int CLOUD_UPLOAD_BATCH_SIZE = 100;
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.sensor.SensorConstants.DataRetention INSTANCE = null;
        
        private DataRetention() {
            super();
        }
    }
    
    /**
     * Battery and device health parameters
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0006\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/project/insole/core/sensor/SensorConstants$DeviceHealth;", "", "()V", "BATTERY_CRITICAL_LEVEL", "", "BATTERY_LOW_LEVEL", "MAX_PACKET_LOSS_PERCENT", "RSSI_CRITICAL_THRESHOLD", "RSSI_GOOD_CONNECTION", "RSSI_WARNING_THRESHOLD", "app_debug"})
    public static final class DeviceHealth {
        public static final int BATTERY_CRITICAL_LEVEL = 10;
        public static final int BATTERY_LOW_LEVEL = 25;
        public static final int RSSI_CRITICAL_THRESHOLD = -100;
        public static final int RSSI_WARNING_THRESHOLD = -85;
        public static final int RSSI_GOOD_CONNECTION = -70;
        public static final int MAX_PACKET_LOSS_PERCENT = 5;
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.sensor.SensorConstants.DeviceHealth INSTANCE = null;
        
        private DeviceHealth() {
            super();
        }
    }
    
    /**
     * Pressure sensor thresholds (in arbitrary pressure units, 0-255 range)
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/project/insole/core/sensor/SensorConstants$PressureThresholds;", "", "()V", "PRESSURE_CRITICAL_THRESHOLD", "", "PRESSURE_HYSTERESIS", "PRESSURE_MAX_VALUE", "PRESSURE_MIN_DETECTABLE", "PRESSURE_WARNING_THRESHOLD", "app_debug"})
    public static final class PressureThresholds {
        public static final int PRESSURE_WARNING_THRESHOLD = 150;
        public static final int PRESSURE_CRITICAL_THRESHOLD = 200;
        public static final int PRESSURE_MIN_DETECTABLE = 10;
        public static final int PRESSURE_MAX_VALUE = 255;
        public static final int PRESSURE_HYSTERESIS = 5;
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.sensor.SensorConstants.PressureThresholds INSTANCE = null;
        
        private PressureThresholds() {
            super();
        }
    }
    
    /**
     * Sampling rates and timing
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0006\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/project/insole/core/sensor/SensorConstants$SamplingRates;", "", "()V", "AGGREGATION_INTERVAL_SECONDS", "", "BLE_MTU_SIZE", "BLE_SENSOR_INTERVAL_MS", "", "BLE_SENSOR_RATE_HZ", "CONNECTION_TIMEOUT_MS", "DATA_BUFFER_SIZE", "MAX_RECONNECTION_ATTEMPTS", "RECONNECTION_INTERVAL_MS", "app_debug"})
    public static final class SamplingRates {
        public static final int BLE_SENSOR_RATE_HZ = 10;
        public static final long BLE_SENSOR_INTERVAL_MS = 100L;
        public static final int BLE_MTU_SIZE = 512;
        public static final long CONNECTION_TIMEOUT_MS = 10000L;
        public static final long RECONNECTION_INTERVAL_MS = 5000L;
        public static final int MAX_RECONNECTION_ATTEMPTS = 5;
        public static final int DATA_BUFFER_SIZE = 1000;
        public static final int AGGREGATION_INTERVAL_SECONDS = 60;
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.sensor.SensorConstants.SamplingRates INSTANCE = null;
        
        private SamplingRates() {
            super();
        }
    }
    
    /**
     * Step detection thresholds and parameters
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/project/insole/core/sensor/SensorConstants$StepDetection;", "", "()V", "MAX_STEP_DURATION_MS", "", "MIN_STEP_DURATION_MS", "PRESSURE_CHANGE_THRESHOLD", "STEP_DETECTION_ZONES", "app_debug"})
    public static final class StepDetection {
        public static final int PRESSURE_CHANGE_THRESHOLD = 20;
        public static final int MIN_STEP_DURATION_MS = 300;
        public static final int MAX_STEP_DURATION_MS = 2000;
        public static final int STEP_DETECTION_ZONES = 5;
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.sensor.SensorConstants.StepDetection INSTANCE = null;
        
        private StepDetection() {
            super();
        }
    }
    
    /**
     * Temperature sensor thresholds (in Celsius)
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0005\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/project/insole/core/sensor/SensorConstants$TemperatureThresholds;", "", "()V", "TEMP_CRITICAL_THRESHOLD", "", "TEMP_MAX_NORMAL", "TEMP_MIN_NORMAL", "TEMP_SENSOR_ERROR_THRESHOLD", "TEMP_WARNING_THRESHOLD", "app_debug"})
    public static final class TemperatureThresholds {
        public static final float TEMP_MIN_NORMAL = 20.0F;
        public static final float TEMP_MAX_NORMAL = 37.0F;
        public static final float TEMP_WARNING_THRESHOLD = 39.0F;
        public static final float TEMP_CRITICAL_THRESHOLD = 41.0F;
        public static final float TEMP_SENSOR_ERROR_THRESHOLD = 50.0F;
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.sensor.SensorConstants.TemperatureThresholds INSTANCE = null;
        
        private TemperatureThresholds() {
            super();
        }
    }
    
    /**
     * Unit conversions
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0003\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/project/insole/core/sensor/SensorConstants$UnitConversions;", "", "()V", "AVERAGE_STEP_LENGTH_CM", "", "PRESSURE_TO_KPA", "PRESSURE_TO_MMHG", "app_debug"})
    public static final class UnitConversions {
        public static final float PRESSURE_TO_MMHG = 0.01F;
        public static final float PRESSURE_TO_KPA = 0.001F;
        public static final float AVERAGE_STEP_LENGTH_CM = 75.0F;
        @org.jetbrains.annotations.NotNull()
        public static final com.project.insole.core.sensor.SensorConstants.UnitConversions INSTANCE = null;
        
        private UnitConversions() {
            super();
        }
    }
}