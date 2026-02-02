package namidevelopment.kiriyaga.api.api.rotation.model;

import namidevelopment.kiriyaga.api.client.RotationsFeature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

import static namidevelopment.kiriyaga.api.NamiApi.*;
import static namidevelopment.kiriyaga.api.util.RotationUtils.*;

/**
 * Rotation request. Used for prioritizing, and controlling motion rotations
 * This is never used for silent (1.20.4-6 exploit) rotations, because they are made as-is and they are very simple
 */
public class RotationRequest {
    /**
     * Identifier of request, it is not unique, we use it as task scoped identifier
     * For example, kill aura id can be KillAuraFeature.getNane().getString()
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

    /**
     * Rotation mode (default = settinga)
     */
    public final RotationsFeature.RotationMode rotationMode;

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
        this(id, priority, yaw, pitch, getDefaultRotationMode());
    }

    /**
     * Static rotation request constructor with override.
     *
     * @param id            identifier
     * @param priority      priority
     * @param yaw           static yaw value
     * @param pitch         static pitch value
     * @param rotationMode  override rotation mode
     */
    public RotationRequest(String id, int priority, float yaw, float pitch, RotationsFeature.RotationMode rotationMode) {
        this.id = id;
        this.priority = priority;
        this.dynamic = false;
        this.targetYaw = yaw;
        this.targetPitch = pitch;
        this.yawSupplier = null;
        this.pitchSupplier = null;
        this.rotationMode = rotationMode;
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
        this(id, priority, yawSupplier, pitchSupplier, getDefaultRotationMode());
    }

    /**
     * Dynamic rotation request constructor with override.
     *
     * @param id             identifier
     * @param priority       priority
     * @param yawSupplier    dynamic yaw supplier
     * @param pitchSupplier  dynamic pitch supplier
     * @param rotationMode   override rotation mode
     */
    public RotationRequest(String id, int priority, Supplier<Float> yawSupplier, Supplier<Float> pitchSupplier, RotationsFeature.RotationMode rotationMode) {
        this.id = id;
        this.priority = priority;
        this.dynamic = true;
        this.yawSupplier = yawSupplier;
        this.pitchSupplier = pitchSupplier;
        this.rotationMode = rotationMode;
        updateTarget();
    }

    /**
     * 1 tick predicted from player eye pos, for motion rotations
     *
     * @param id  Identifier
     * @param priority   Priority
     * @param player Player BEFORE motion predict
     * @param pos  Pos to look at
     */
    public RotationRequest(String id, int priority, LivingEntity player, Vec3 pos) {
        this.id = id;
        this.priority = priority;
        this.rotationMode = RotationsFeature.RotationMode.MOTION;

        Vec3 predictedEye = predictMotion(player);

        this.targetYaw = getYawToVec(predictedEye, pos);
        this.targetPitch = getPitchToVec(predictedEye, pos);

        this.yawSupplier = null;
        this.pitchSupplier = null;
        dynamic = false;
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

    private static RotationsFeature.RotationMode getDefaultRotationMode() {
        RotationsFeature Feature = FEATURE_SERVICE.getStorage().getByClass(RotationsFeature.class);
        return Feature != null ? Feature.rotation.get() : RotationsFeature.RotationMode.MOTION;
    }
}