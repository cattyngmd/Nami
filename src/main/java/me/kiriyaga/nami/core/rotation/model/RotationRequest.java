package me.kiriyaga.nami.core.rotation.model;

import java.util.function.Supplier;

/**
 * Rotation request. Used for prioritizing, and controlling motion rotations
 * This is never used for silent (1.20.4-6 exploit) rotations, because they are made as-is and they are very simple
 */
public class RotationRequest {
    /**
     * Identifier of request, it is not unique, we use it as task scoped identifier
     * For example, kill aura id can be KillAuraModule.getNane().getString()
     * Any new request with the same id will replace old one
     * Do not replace supplier requests! You should control their life cycle by yourself!
     */
    public final String id;

    /**
     * Priority of rotation
     * Theesde are also recalculate:
     * New request appeared
     * Reequest finished
     */
    public final int priority;

    /**
     * Float supplier:
     * <ul>
     *     <li>{@code false} — angles are declared one time, static;</li>
     *     <li>{@code true} — (YOU SHOULD CONTROL THEIR LIFE CYCLE BY YOURSELF!)Angles dynamic updated by {@link Supplier}.</li>
     * </ul>
     */
    private final boolean dynamic;

    /**
     * Supplir of yaw param, dynamic
     */
    private final Supplier<Float> yawSupplier;

    /**
     * Supplier of pitch param, dynamic.
     */
    private final Supplier<Float> pitchSupplier;

    /**
     * Static yaw param.
     */
    public float targetYaw;

    /**
     * dynamic yaw param.
     */
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

    /**
     * Static rotation request constructor.
     *
     * @param id        identifier
     * @param priority  priority
     * @param yaw       static yaw value
     * @param pitch     static pitch value
     */
    public RotationRequest(String id, int priority, float yaw, float pitch) {
        this.id = id;
        this.priority = priority;
        this.dynamic = false;
        this.targetYaw = yaw;
        this.targetPitch = pitch;
        this.yawSupplier = null;
        this.pitchSupplier = null;
    }

    /**
     * Dynamic rotation request constructor.
     *
     * @param id             identifier
     * @param priority       priority
     * @param yawSupplier    dynamic yaw supplier
     * @param pitchSupplier  dynamic pitch supplier
     */
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

    /**
     * Updates {@link #targetYaw} and {@link #targetPitch},
     * if request is dynamic and has valid {@link Supplier}.
     */
    public void updateTarget() {
        if (dynamic && yawSupplier != null && pitchSupplier != null) {
            targetYaw = yawSupplier.get();
            targetPitch = pitchSupplier.get();
        }
    }
}