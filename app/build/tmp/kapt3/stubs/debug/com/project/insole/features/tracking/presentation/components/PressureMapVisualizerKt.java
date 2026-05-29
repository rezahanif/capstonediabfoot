package com.project.insole.features.tracking.presentation.components;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0016\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\u001a\u0010\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0003\u001a\u001c\u0010\u0004\u001a\u00020\u00012\u0012\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00030\u00060\u0006H\u0007\u00a8\u0006\u0007"}, d2 = {"PressureCell", "", "zone", "Lcom/project/insole/features/tracking/domain/model/PressureZone;", "PressureMapVisualizer", "pressureGrid", "", "app_debug"})
public final class PressureMapVisualizerKt {
    
    /**
     * Stateless composable that draws the heatmap visualization of foot pressure.
     * Receives pressure grid data and renders colored cells based on pressure intensity.
     */
    @androidx.compose.runtime.Composable()
    public static final void PressureMapVisualizer(@org.jetbrains.annotations.NotNull()
    java.util.List<? extends java.util.List<com.project.insole.features.tracking.domain.model.PressureZone>> pressureGrid) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void PressureCell(com.project.insole.features.tracking.domain.model.PressureZone zone) {
    }
}