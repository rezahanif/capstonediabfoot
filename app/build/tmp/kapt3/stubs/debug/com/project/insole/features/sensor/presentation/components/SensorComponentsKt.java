package com.project.insole.features.sensor.presentation.components;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000.\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0006\n\u0002\u0010\u0007\n\u0002\b\u0002\u001a\u001a\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u0010\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\bH\u0007\u001a\u001a\u0010\t\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u0016\u0010\n\u001a\u00020\u00012\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00030\fH\u0007\u001a \u0010\r\u001a\u00020\u00012\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00030\f2\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u001a\u0010\u000e\u001a\u00020\u00012\u0006\u0010\u000f\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u0010\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u000f\u001a\u00020\u0003H\u0007\u001a\u001a\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0012\u001a\u00020\u00132\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u0010\u0010\u0014\u001a\u00020\u00012\u0006\u0010\u0012\u001a\u00020\u0013H\u0007\u00a8\u0006\u0015"}, d2 = {"BatteryStatusCard", "", "battery", "", "modifier", "Landroidx/compose/ui/Modifier;", "ConnectionQualityGraph", "quality", "", "ConnectionStatusCard", "FsrDetailedGrid", "fsrValues", "", "FsrStatusCard", "StepsCard", "steps", "StepsTrendChart", "TemperatureCard", "temp", "", "TemperatureDetailChart", "app_debug"})
public final class SensorComponentsKt {
    
    /**
     * Summary card showing FSR pressure status (Good/Bad).
     * If ANY FSR > threshold → Bad, else Good.
     */
    @androidx.compose.runtime.Composable()
    public static final void FsrStatusCard(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> fsrValues, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Summary card showing battery status.
     */
    @androidx.compose.runtime.Composable()
    public static final void BatteryStatusCard(int battery, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Summary card showing connection quality status.
     */
    @androidx.compose.runtime.Composable()
    public static final void ConnectionStatusCard(@org.jetbrains.annotations.NotNull()
    java.lang.String quality, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Summary card showing temperature status.
     */
    @androidx.compose.runtime.Composable()
    public static final void TemperatureCard(float temp, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Summary card showing daily steps.
     */
    @androidx.compose.runtime.Composable()
    public static final void StepsCard(int steps, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Detailed grid showing each FSR sensor value independently.
     */
    @androidx.compose.runtime.Composable()
    public static final void FsrDetailedGrid(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> fsrValues) {
    }
    
    /**
     * Detailed temperature chart.
     */
    @androidx.compose.runtime.Composable()
    public static final void TemperatureDetailChart(float temp) {
    }
    
    /**
     * Detailed connection quality graph.
     */
    @androidx.compose.runtime.Composable()
    public static final void ConnectionQualityGraph(@org.jetbrains.annotations.NotNull()
    java.lang.String quality) {
    }
    
    /**
     * Detailed steps trend chart.
     */
    @androidx.compose.runtime.Composable()
    public static final void StepsTrendChart(int steps) {
    }
}