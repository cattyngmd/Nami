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
    private int ticksBeforeRelease;

    private boolean returned = false;

    private int ticksHolding = 0;

    private boolean cursorLocked = false;

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
        if (activeRequest != null) return true;

        for (RotationRequest request : requests) {
            if (Math.abs(wrapDegrees(request.targetYaw - rotationYaw)) > rotationThreshold ||
                    Math.abs(request.targetPitch - rotationPitch) > rotationThreshold) {
                return true;
            }
        }

        return false;
    }



    public float getRenderYaw() {
        return realYaw;
    }

    public float getRenderPitch() {
        return realPitch;
    }

    public void setRenderYaw(float realYaw) {
        this.realYaw = realYaw;
    }

    public void setRenderPitch(float realPitch) {
        this.realPitch = realPitch;
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

        RotationManagerModule rotationManagerModule = MODULE_MANAGER.getModule(RotationManagerModule.class);
        rotationSpeed = rotationManagerModule.rotationSpeed.get().floatValue();
        rotationEaseFactor = rotationManagerModule.rotationEaseFactor.get().floatValue();
        rotationThreshold = rotationManagerModule.rotationThreshold.get().floatValue();
        ticksBeforeRelease = rotationManagerModule.ticksBeforeRelease.get();

        boolean hasRequests = !requests.isEmpty();

        if (hasRequests) {
            returned = true;

            if (!cursorLocked && !MINECRAFT.mouse.isCursorLocked()) {
                MINECRAFT.mouse.lockCursor();
                cursorLocked = true;
            }

            if (activeRequest == null || !requests.contains(activeRequest)) {
                activeRequest = requests.get(0);
                currentYawSpeed = 0f;
                currentPitchSpeed = 0f;
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

            boolean rotationReached = Math.abs(yawDiff) < rotationThreshold && Math.abs(pitchDiff) < rotationThreshold;

            if (rotationReached) {
                ticksHolding++;
                if (ticksHolding >= ticksBeforeRelease) {
                    requests.remove(activeRequest);
                    activeRequest = null;
                    ticksHolding = 0;
                }
            } else {
                ticksHolding = 0;
            }
        } else if (returned){
            returned = false;
            rotationYaw = realYaw;
            rotationPitch = realPitch;
            applyRotationToPlayer();

            activeRequest = null;
            ticksHolding = 0;

            if (cursorLocked && MINECRAFT.mouse.isCursorLocked()) {
                MINECRAFT.mouse.unlockCursor();
                cursorLocked = false;
            }
        }
        // no interp
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

    private float getWrappedYawDiff(float from, float to) {
        float diff = wrapDegrees(to - from);
        return diff;
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
