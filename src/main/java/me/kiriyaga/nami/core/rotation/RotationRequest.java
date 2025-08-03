package me.kiriyaga.nami.core.rotation;

import java.util.function.Supplier;

public class RotationRequest {
    public final String id;
    public final int priority;
    private final boolean dynamic;
    private final Supplier<Float> yawSupplier;
    private final Supplier<Float> pitchSupplier;

    public float targetYaw;
    public float targetPitch;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RotationRequest)) return false;
        RotationRequest other = (RotationRequest) o;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public RotationRequest(String id, int priority, float yaw, float pitch) {
        this.id = id;
        this.priority = priority;
        this.dynamic = false;
        this.targetYaw = yaw;
        this.targetPitch = pitch;
        this.yawSupplier = null;
        this.pitchSupplier = null;
    }

    public RotationRequest(String id, int priority, Supplier<Float> yawSupplier, Supplier<Float> pitchSupplier) {
        this.id = id;
        this.priority = priority;
        this.dynamic = true;
        this.yawSupplier = yawSupplier;
        this.pitchSupplier = pitchSupplier;
        updateTarget();
    }

    public boolean shouldUpdate() {
        return dynamic;
    }

    public void updateTarget() {
        if (dynamic && yawSupplier != null && pitchSupplier != null) {
            targetYaw = yawSupplier.get();
            targetPitch = pitchSupplier.get();
        }
    }
}