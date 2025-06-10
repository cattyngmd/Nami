package me.kiriyaga.essentials.manager;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.UpdateEvent;
import me.kiriyaga.essentials.feature.module.impl.client.RotationManagerModule;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.function.Supplier;

import static me.kiriyaga.essentials.Essentials.*;

public class RotationManager {
    private final List<RotationRequest> requests = new ArrayList<>();
    private RotationRequest activeRequest = null;

    private float realYaw, realPitch;
    private float rotationYaw, rotationPitch;
    private float currentYawSpeed = 0f;
    private float currentPitchSpeed = 0f;

    private float rotationSpeed;
    private float rotationEaseFactor;
    private float rotationThreshold;

    public void init() {
        EVENT_MANAGER.register(this);
    }

    public void updateRealRotation(float yaw, float pitch) {
        realYaw = wrapDegrees(yaw);
        realPitch = MathHelper.clamp(pitch, -90f, 90f);
    }

    public void submitRequest(RotationRequest request) {
        requests.removeIf(r -> Objects.equals(r.id, request.id));
        requests.add(request);
        requests.sort(Comparator.comparingInt(r -> -r.priority));
    }

    public void cancelRequest(String id) {
        requests.removeIf(r -> Objects.equals(r.id, id));
    }

    public boolean isRotating() {
        return activeRequest != null;
    }

    public float getRenderYaw() {
        return realYaw;
    }

    public float getRenderPitch() {
        return realPitch;
    }

    public float getRotationYaw() {
        return rotationYaw;
    }

    public float getRotationPitch() {
        return rotationPitch;
    }

    private void applyRotationToPlayer() {
        if (MINECRAFT.player != null) {
            MINECRAFT.player.setYaw(rotationYaw);
            MINECRAFT.player.setPitch(rotationPitch);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUpdate(UpdateEvent event) {
        if (MINECRAFT.player == null) return;

        if (requests.isEmpty()) {
            activeRequest = null;
            return;
        }

        RotationManagerModule rotationManagerModule = MODULE_MANAGER.getModule(RotationManagerModule.class);
        rotationSpeed = rotationManagerModule.rotationSpeed.get().floatValue();
        rotationEaseFactor = rotationManagerModule.rotationEaseFactor.get().floatValue();
        rotationThreshold = rotationManagerModule.rotationThreshold.get().floatValue();

        if (activeRequest == null || !requests.contains(activeRequest)) {
            activeRequest = requests.get(0);
            currentYawSpeed = 0;
            currentPitchSpeed = 0;
            rotationYaw = MINECRAFT.player.getYaw();
            rotationPitch = MINECRAFT.player.getPitch();
        }

        if (activeRequest.shouldUpdate()) {
            activeRequest.updateTarget();
        }

        float yawDiff = wrapDegrees(activeRequest.targetYaw - rotationYaw);
        float pitchDiff = activeRequest.targetPitch - rotationPitch;

        currentYawSpeed = lerp(currentYawSpeed, yawDiff, rotationEaseFactor);
        currentPitchSpeed = lerp(currentPitchSpeed, pitchDiff, rotationEaseFactor);

        rotationYaw = wrapDegrees(rotationYaw + MathHelper.clamp(currentYawSpeed, -rotationSpeed, rotationSpeed));
        rotationPitch += MathHelper.clamp(currentPitchSpeed, -rotationSpeed, rotationSpeed);

        applyRotationToPlayer();

        if (Math.abs(yawDiff) < rotationThreshold && Math.abs(pitchDiff) < rotationThreshold) {
            requests.remove(activeRequest);
            activeRequest = null;
        }
    }

    private float wrapDegrees(float angle) {
        angle %= 360f;
        if (angle >= 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }

    private float lerp(float from, float to, float factor) {
        return from + (to - from) * factor;
    }

    public static class RotationRequest {
        public final String id;
        public final int priority;
        private final boolean dynamic;
        private final Supplier<Float> yawSupplier;
        private final Supplier<Float> pitchSupplier;

        public float targetYaw;
        public float targetPitch;

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
}
