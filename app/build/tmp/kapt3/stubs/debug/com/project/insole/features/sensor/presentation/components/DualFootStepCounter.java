package com.project.insole.features.sensor.presentation.components;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u001d\u0010\u0015\u001a\u0004\u0018\u00010\u00122\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019\u00a2\u0006\u0002\u0010\u001aJ\u000e\u0010\u001b\u001a\u00020\u00122\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010\u001e\u001a\u00020\u00122\u0006\u0010\u001c\u001a\u00020\u001dJ\u0006\u0010\u001f\u001a\u00020 R\u0011\u0010\u0003\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u0011\u0010\u0007\u001a\u00020\b8F\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\nR\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u000f\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000eR\u0011\u0010\u0011\u001a\u00020\u00128F\u00a2\u0006\u0006\u001a\u0004\b\u0013\u0010\u0014\u00a8\u0006!"}, d2 = {"Lcom/project/insole/features/sensor/presentation/components/DualFootStepCounter;", "", "()V", "combinedAccelMag", "", "getCombinedAccelMag", "()F", "dominantState", "Lcom/project/insole/features/sensor/presentation/components/WalkState;", "getDominantState", "()Lcom/project/insole/features/sensor/presentation/components/WalkState;", "leftFSM", "Lcom/project/insole/features/sensor/presentation/components/WalkingFSM;", "getLeftFSM", "()Lcom/project/insole/features/sensor/presentation/components/WalkingFSM;", "rightFSM", "getRightFSM", "totalSteps", "", "getTotalSteps", "()I", "processBleString", "raw", "", "isLeft", "", "(Ljava/lang/String;Z)Ljava/lang/Integer;", "processLeft", "packet", "Lcom/project/insole/features/sensor/domain/model/SensorPacket;", "processRight", "reset", "", "app_debug"})
public final class DualFootStepCounter {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.presentation.components.WalkingFSM leftFSM = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.sensor.presentation.components.WalkingFSM rightFSM = null;
    
    public DualFootStepCounter() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.features.sensor.presentation.components.WalkingFSM getLeftFSM() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.features.sensor.presentation.components.WalkingFSM getRightFSM() {
        return null;
    }
    
    public final int getTotalSteps() {
        return 0;
    }
    
    /**
     * Feed a pre-parsed packet from the LEFT insole. Returns updated total.
     */
    public final int processLeft(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.domain.model.SensorPacket packet) {
        return 0;
    }
    
    /**
     * Feed a pre-parsed packet from the RIGHT insole. Returns updated total.
     */
    public final int processRight(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.sensor.domain.model.SensorPacket packet) {
        return 0;
    }
    
    /**
     * Convenience helper to process raw string.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer processBleString(@org.jetbrains.annotations.NotNull()
    java.lang.String raw, boolean isLeft) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.features.sensor.presentation.components.WalkState getDominantState() {
        return null;
    }
    
    public final float getCombinedAccelMag() {
        return 0.0F;
    }
    
    public final void reset() {
    }
}