package me.kiriyaga.nami.core.rotation;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.impl.client.RotationManagerModule;
import net.minecraft.util.math.MathHelper;

import static me.kiriyaga.nami.Nami.*;

public class RotationTickHandler {

    private final RotationStateHandler stateHandler;
    private final RotationRequestHandler requestHandler;

    private float rotationSpeed;
    private float rotationEaseFactor;
    private float rotationThreshold;
    private int ticksBeforeRelease;
    private float jitterAmount;
    private float jitterSpeed;
    private float currentYawSpeed = 0f, currentPitchSpeed = 0f;
    private int ticksHolding = 0;
    private boolean returning = false;
    private int tickCount = 0;

    public RotationTickHandler(RotationStateHandler stateHandler, RotationRequestHandler requestHandler) {
        this.stateHandler = stateHandler;
        this.requestHandler = requestHandler;
    }

    public void init() {
        EVENT_MANAGER.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null) return;

        RotationManagerModule module = MODULE_MANAGER.getStorage().getByClass(RotationManagerModule.class);
        loadSettings(module);
        stateHandler.updateRealRotation(MC.player.getYaw(), MC.player.getPitch());

        RotationRequest active = requestHandler.getActiveRequest();
        if (active != null) {
            processRequest(active);
        } else if (returning) {
            returnToRealRotation();
        } else {
            idleReset();
        }

        tickCount++;
    }

    private void loadSettings(RotationManagerModule module) {
        rotationSpeed = module.rotationSpeed.get().floatValue();
        rotationEaseFactor = module.rotationEaseFactor.get().floatValue();
        rotationThreshold = module.rotationThreshold.get().floatValue();
        ticksBeforeRelease = module.ticksBeforeRelease.get();
        jitterAmount = module.jitterAmount.get().floatValue();
        jitterSpeed = module.jitterSpeed.get().floatValue();
    }

    private void processRequest(RotationRequest request) {
        if (!request.id.equals(requestHandler.getLastActiveId())) {
            resetRotationToReal();
            requestHandler.setLastActiveId(request.id);
        }

        boolean updated = false;
        if (request.shouldUpdate()) {
            float oldYaw = request.targetYaw;
            float oldPitch = request.targetPitch;
            request.updateTarget();
            updated = Math.abs(oldYaw - request.targetYaw) > 0.001f || Math.abs(oldPitch - request.targetPitch) > 0.001f;
            if (updated) ticksHolding = 0;
        }

        float yawDiff = wrapDegrees(request.targetYaw - stateHandler.getRotationYaw());
        float pitchDiff = request.targetPitch - stateHandler.getRotationPitch();

        boolean reached = Math.abs(yawDiff) <= rotationThreshold && Math.abs(pitchDiff) <= rotationThreshold;

        if (reached && !updated) {
            if (++ticksHolding >= ticksBeforeRelease) {
                requestHandler.removeActiveRequest();
                ticksHolding = 0;
                returning = true;
            }
        } else {
            ticksHolding = 0;
            interpolateRotation(yawDiff, pitchDiff);
        }

        if (ticksHolding > 0 && jitterAmount > 0) {
            applyJitter();
        }
    }

    private void resetRotationToReal() {
        stateHandler.setRotationYaw(stateHandler.getRealYaw());
        stateHandler.setRotationPitch(stateHandler.getRealPitch());
        currentYawSpeed = 0f;
        currentPitchSpeed = 0f;
        ticksHolding = 0;
        returning = false;
    }

    private void returnToRealRotation() {
        float yawDiff = wrapDegrees(stateHandler.getRealYaw() - stateHandler.getRotationYaw());
        float pitchDiff = stateHandler.getRealPitch() - stateHandler.getRotationPitch();

        interpolateRotation(yawDiff, pitchDiff);

        boolean backReached = Math.abs(yawDiff) <= rotationThreshold && Math.abs(pitchDiff) <= rotationThreshold;
        if (backReached) {
            returning = false;
            stateHandler.setRotationYaw(stateHandler.getRealYaw());
            stateHandler.setRotationPitch(stateHandler.getRealPitch());
            requestHandler.clearLastActiveId();
        }
    }

    private void interpolateRotation(float yawDiff, float pitchDiff) {
        currentYawSpeed = lerp(currentYawSpeed, yawDiff, rotationEaseFactor);
        currentPitchSpeed = lerp(currentPitchSpeed, pitchDiff, rotationEaseFactor);

        float yawSpeed = MathHelper.clamp(currentYawSpeed, -rotationSpeed, rotationSpeed);
        float pitchSpeed = MathHelper.clamp(currentPitchSpeed, -rotationSpeed, rotationSpeed);

        stateHandler.setRotationYaw(stateHandler.getRotationYaw() + yawSpeed);
        stateHandler.setRotationPitch(stateHandler.getRotationPitch() + pitchSpeed);
    }

    private void applyJitter() {
        float yawOffset = jitterAmount * (float) Math.sin(tickCount * jitterSpeed);
        float pitchOffset = jitterAmount * (float) Math.cos(tickCount * jitterSpeed);

        stateHandler.setRotationYaw(stateHandler.getRotationYaw() + yawOffset);
        stateHandler.setRotationPitch(stateHandler.getRotationPitch() + pitchOffset);
    }

    private void idleReset() {
        requestHandler.clearLastActiveId();
        stateHandler.setRotationYaw(stateHandler.getRealYaw());
        stateHandler.setRotationPitch(stateHandler.getRealPitch());
        currentYawSpeed = 0f;
        currentPitchSpeed = 0f;
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

    public boolean isRotating() {
        return requestHandler.getActiveRequest() != null || returning;
    }
}
