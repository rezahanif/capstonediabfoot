package com.project.insole.features.sensor.presentation.components;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000F\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\n\u001a0\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u00012\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\n2\b\b\u0002\u0010\f\u001a\u00020\rH\u0003\u001aJ\u0010\u000e\u001a\u00020\u00062\b\u0010\u000f\u001a\u0004\u0018\u00010\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u00102\u0006\u0010\u0012\u001a\u00020\n2\u0006\u0010\u0013\u001a\u00020\n2\b\b\u0002\u0010\u0014\u001a\u00020\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u00152\b\b\u0002\u0010\f\u001a\u00020\rH\u0007\u001a\u0012\u0010\u0017\u001a\u00020\u00062\b\b\u0002\u0010\f\u001a\u00020\rH\u0003\u001a \u0010\u0018\u001a\u00020\u00062\u0006\u0010\u0019\u001a\u00020\u00102\u0006\u0010\u001a\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\nH\u0003\u001a \u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\b2\u0006\u0010\u001e\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0002\u001a\u0016\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\b0\u00012\u0006\u0010 \u001a\u00020\bH\u0002\u001a\u0015\u0010!\u001a\u00020\u00022\u0006\u0010\"\u001a\u00020\bH\u0002\u00a2\u0006\u0002\u0010#\u001a\u0012\u0010$\u001a\u00020\b2\b\u0010%\u001a\u0004\u0018\u00010\u0010H\u0002\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00040\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006&"}, d2 = {"HEATMAP_COLORS", "", "Landroidx/compose/ui/graphics/Color;", "LEFT_ZONES", "Lcom/project/insole/features/sensor/presentation/components/SensorZone;", "FootHeatmap", "", "zoneIntensities", "", "mirrored", "", "connected", "modifier", "Landroidx/compose/ui/Modifier;", "PlantarPressureCard", "rawBleLeft", "", "rawBleRight", "leftConnected", "rightConnected", "leftPacketSeq", "", "rightPacketSeq", "PressureLegend", "PressureReadout", "label", "value", "buildFootPath", "Landroidx/compose/ui/graphics/Path;", "w", "h", "distributeFromSingle", "rawValue", "heatColor", "intensity", "(F)J", "parsePressure", "raw", "app_debug"})
public final class PlantarPressureCardKt {
    
    /**
     * Anatomical sensor positions for a LEFT foot.
     * Right foot zones are mirrored horizontally inside [FootHeatmap].
     *
     * Y=0 is the TOE end, Y=1 is the HEEL end  (matching the foot silhouette
     * path which is drawn top=toes, bottom=heel).
     */
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<com.project.insole.features.sensor.presentation.components.SensorZone> LEFT_ZONES = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<androidx.compose.ui.graphics.Color> HEATMAP_COLORS = null;
    
    /**
     * Convert a single FSR reading (0..255) into per-zone intensity values
     * using the anatomical weight ratios.  Each zone gets  value × weight.
     *
     * When you upgrade to 5 sensors, replace this with:
     *  fun directMapping(values: List<Float>) = values   (one-to-one)
     */
    private static final java.util.List<java.lang.Float> distributeFromSingle(float rawValue) {
        return null;
    }
    
    private static final long heatColor(float intensity) {
        return 0L;
    }
    
    private static final androidx.compose.ui.graphics.Path buildFootPath(float w, float h, boolean mirrored) {
        return null;
    }
    
    /**
     * Draws one foot with a pressure heatmap overlay.
     *
     * @param zoneIntensities  List of 5 floats in [0..1], one per sensor zone.
     *                        Index order matches [LEFT_ZONES].
     * @param mirrored         true = right foot (reflects X axis).
     * @param connected        false = BLE device not connected; shows a greyed-out
     *                        silhouette with a dashed border and a "No Signal" label.
     */
    @androidx.compose.runtime.Composable()
    private static final void FootHeatmap(java.util.List<java.lang.Float> zoneIntensities, boolean mirrored, boolean connected, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void PressureLegend(androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void PlantarPressureCard(@org.jetbrains.annotations.Nullable()
    java.lang.String rawBleLeft, @org.jetbrains.annotations.Nullable()
    java.lang.String rawBleRight, boolean leftConnected, boolean rightConnected, long leftPacketSeq, long rightPacketSeq, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Extract the pressure value (field index 6) from a raw BLE string.
     */
    private static final float parsePressure(java.lang.String raw) {
        return 0.0F;
    }
    
    @androidx.compose.runtime.Composable()
    private static final void PressureReadout(java.lang.String label, float value, boolean connected) {
    }
}