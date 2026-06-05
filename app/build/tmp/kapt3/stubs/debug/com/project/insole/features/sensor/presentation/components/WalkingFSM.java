package com.project.insole.features.sensor.presentation.components;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\t\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010!\u001a\u00020\u00042\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\u0004H\u0002J\u000e\u0010$\u001a\u00020\u001a2\u0006\u0010%\u001a\u00020&J\u0006\u0010\'\u001a\u00020(R\u001e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u0004@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u0004@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0007R\u000e\u0010\u0012\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0017\u001a\u00020\u00162\u0006\u0010\u0003\u001a\u00020\u0016@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u001e\u0010\u001b\u001a\u00020\u001a2\u0006\u0010\u0003\u001a\u00020\u001a@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u000e\u0010\u001e\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006)"}, d2 = {"Lcom/project/insole/features/sensor/presentation/components/WalkingFSM;", "", "()V", "<set-?>", "", "accelMag", "getAccelMag", "()F", "alpha", "axF", "ayF", "azF", "filterInit", "", "gxF", "gyF", "gyroMag", "getGyroMag", "gzF", "previousTotalAccel", "standingTimerMs", "", "Lcom/project/insole/features/sensor/presentation/components/WalkState;", "state", "getState", "()Lcom/project/insole/features/sensor/presentation/components/WalkState;", "", "stepCount", "getStepCount", "()I", "stepThreshold", "stepTriggered", "walkingTimerMs", "ema", "input", "prev", "process", "packet", "Lcom/project/insole/features/sensor/domain/model/SensorPacket;", "reset", "", "app_debug"})
public final class WalkingFSM {
    private final float alpha = 0.2F;
    private boolean filterInit = false;
    private float axF = 0.0F;
    private float ayF = 0.0F;
    private float azF = 0.0F;
    private float gxF = 0.0F;
    private float gyF = 0.0F;
    private float gzF = 0.0F;
    private float accelMag = 0.0F;
    private float gyroMag = 0.0F;
    private float previousTotalAccel = 1.0F;
    @org.jetbrains.annotations.NotNull()
    private com.project.insole.features.sensor.presentation.components.WalkState state = com.project.insole.features.sensor.presentation.components.WalkState.STANDING;
    private long walkingTimerMs = 0L;
    private long standingTimerMs = 0L;
    private int stepCount = 0;
    private boolean stepTriggered = false;
    private final float stepThreshold = 0.1F;
    
    public WalkingFSM() {
        super();
    }
    
    public final float getAccelMag() {
        return 0.0F;
    }
    
    public final float getGyroMag() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.features.sensor.presentation.components.WalkState getState() {
        return null;
    }
    
    public final int getStepCount() {
        return 0;
    }
    
    private final float ema(float input, float prev) {
        return 0.0F;
    }
    
    public final int process(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.domain.model.SensorPacket packet) {
        return 0;
    }
    
    public final void reset() {
    }
}