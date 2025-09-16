package me.kiriyaga.nami.core.rotation;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.KeyInputEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.impl.client.RotationModule;
import me.kiriyaga.nami.feature.module.impl.movement.GuiMoveModule;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import me.kiriyaga.nami.util.InputCache;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.core.rotation.RotationStateHandler.wrapDegrees;

public class RotationTickHandler {

    private final RotationStateHandler stateHandler;
    private final RotationRequestHandler requestHandler;

    private float rotationSpeed;
    private float rotationEaseFactor;
    private float rotationThreshold;
    private int ticksBeforeRelease;
//    private float jitterAmount;
//    private float jitterSpeed;
    private float currentYawSpeed = 0f, currentPitchSpeed = 0f;
    private int ticksHolding = 0;
    private boolean returning = false;
    private int tickCount = 0;
    private boolean forwardPressed = false;
    private boolean leftPressed = false;
    private boolean backPressed = false;
    private boolean rightPressed = false;

    public RotationTickHandler(RotationStateHandler stateHandler, RotationRequestHandler requestHandler) {
        this.stateHandler = stateHandler;
        this.requestHandler = requestHandler;
    }

    public void init() {
        EVENT_MANAGER.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyInput(KeyInputEvent event) {
        int key = event.key;
        int action = event.action;
        int scancode = event.scancode;

        updateHeld(MC.options.forwardKey, key, scancode, action, false, v -> forwardPressed = v);
        updateHeld(MC.options.leftKey,    key, scancode, action, false, v -> leftPressed = v);
        updateHeld(MC.options.backKey,    key, scancode, action, false, v -> backPressed = v);
        updateHeld(MC.options.rightKey,   key, scancode, action, false, v -> rightPressed = v);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null) return;

        RotationModule module = MODULE_MANAGER.getStorage().getByClass(RotationModule.class);
        loadSettings(module);
        stateHandler.updateRealRotation(MC.player.getYaw(), MC.player.getPitch());
        interpolateRenderRotation();

        RotationRequest active = requestHandler.getActiveRequest();
        if (active != null) {
            processRequest(active);
        } else if (returning) {
            returnToRealRotation();
        } else {
            idleReset();
        }

        if (module.moveFix.get() && stateHandler.isRotating())
            fixMovementForSpoof();

        tickCount++;
    }

    private void fixMovementForSpoof() {
        if (MC.player == null) return;

        float realYaw = MC.player.getYaw();
        float spoofYaw = stateHandler.getRotationYaw();
        float delta = MathHelper.wrapDegrees(realYaw - spoofYaw);

        // theese are tick thread and render thread
        boolean forward = forwardPressed;
        boolean back = backPressed;
        boolean left = leftPressed;
        boolean right = rightPressed;

        InputCache.update(
                forward,
                back,
                left,
                right
        );

        float inputX = (right ? 1 : 0) - (left ? 1 : 0);
        float inputZ = (forward ? 1 : 0) - (back ? 1 : 0);

        MC.options.forwardKey.setPressed(false);
        MC.options.backKey.setPressed(false);
        MC.options.leftKey.setPressed(false);
        MC.options.rightKey.setPressed(false);

        if (inputX == 0 && inputZ == 0) return;

        double moveAngle = Math.toDegrees(Math.atan2(inputX, inputZ));
        double finalAngle = moveAngle + delta;
        int sector = (int) Math.round(finalAngle / 45.0) & 7;

        // i hate myself its 02:28
        switch (sector) {
            case 0: MC.options.forwardKey.setPressed(true); break;
            case 1: MC.options.forwardKey.setPressed(true); MC.options.rightKey.setPressed(true); break;
            case 2: MC.options.rightKey.setPressed(true); break;
            case 3: MC.options.backKey.setPressed(true); MC.options.rightKey.setPressed(true); break;
            case 4: MC.options.backKey.setPressed(true); break;
            case 5: MC.options.backKey.setPressed(true); MC.options.leftKey.setPressed(true); break;
            case 6: MC.options.leftKey.setPressed(true); break;
            case 7: MC.options.forwardKey.setPressed(true); MC.options.leftKey.setPressed(true); break;
        }
    }

    private void loadSettings(RotationModule module) {
        rotationSpeed = module.rotationSpeed.get().floatValue();
        rotationEaseFactor = module.rotationEaseFactor.get().floatValue();
        rotationThreshold = module.rotationThreshold.get().floatValue();
        ticksBeforeRelease = module.ticksBeforeRelease.get();
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

        float yawDiff = yawDifference(request.targetYaw, stateHandler.getRotationYaw());
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
    }

    // in resetRotationToReal() returnToRealRotation() we need to set player yaw, clamped to closest to rotation yaw
    // we need to do this between rotation requests change (highest priority appeared when old one not finished)
    // and when rotation is ended
    // this is made to prevent yaw jump
    private void resetRotationToReal() {
        float targetYaw = alignYaw(stateHandler.getRealYaw(), stateHandler.getRotationYaw());
        stateHandler.updateRealRotation(targetYaw, stateHandler.getRealPitch());
        MC.player.setYaw(targetYaw);
        stateHandler.setRotationYaw(stateHandler.getRealYaw());
        stateHandler.setRotationPitch(stateHandler.getRealPitch());
        currentYawSpeed = 0f;
        currentPitchSpeed = 0f;
        ticksHolding = 0;
        returning = false;
    }

    private void returnToRealRotation() {
        float targetYaw = alignYaw(stateHandler.getRealYaw(), stateHandler.getRotationYaw());
        float yawDiff = targetYaw - stateHandler.getRotationYaw();
        float pitchDiff = stateHandler.getRealPitch() - stateHandler.getRotationPitch();

        interpolateRotation(yawDiff, pitchDiff);

        boolean backReached = Math.abs(yawDiff) <= rotationThreshold && Math.abs(pitchDiff) <= rotationThreshold;
        if (backReached) {
            returning = false;
            stateHandler.updateRealRotation(targetYaw, stateHandler.getRealPitch());
            MC.player.setYaw(targetYaw);
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

        float newYaw = stateHandler.getRotationYaw() + yawSpeed;
        float newPitch = stateHandler.getRotationPitch() + pitchSpeed;

//        RotationManagerModule module = MODULE_MANAGER.getStorage().getByClass(RotationManagerModule.class);
//        if (module.mouseDeltaFix.get()) { // https://github.com/GrimAnticheat/Grim/blob/57a9f8f432800382d43c28df9e8409b4d7d80813/common/src/main/java/ac/grim/grimac/checks/impl/aim/AimModulo360.java#L31
//            float lastServerYaw = stateHandler.getServerYaw();
//            float lastServerDeltaYaw = stateHandler.getServerDeltaYaw();
//
//            float rawDiff = yawDifference(newYaw, lastServerYaw);
//
//            MODULE_MANAGER.getStorage().getByClass(Debug.class).debugDelta(Text.of(
//                    "before wrap: newYaw=" + newYaw +
//                            ", lastServerYaw=" + lastServerYaw +
//                            ", lastServerDeltaYaw=" + lastServerDeltaYaw +
//                            ", rawDiff=" + rawDiff
//            ));
//
//            while (rawDiff > 360f) rawDiff -= 360f;
//            while (rawDiff < -360f) rawDiff += 360f;
//
//            MODULE_MANAGER.getStorage().getByClass(Debug.class).debugDelta(Text.of(
//                    "After wrap: rawDiff=" + rawDiff
//            ));
//
//            if (Math.abs(rawDiff) > 320f && Math.abs(lastServerDeltaYaw) < 30f) {
//                newYaw = lastServerYaw + Math.copySign(319f, rawDiff);
//                MODULE_MANAGER.getStorage().getByClass(Debug.class).debugDelta(Text.of(
//                        "clipping applied: newYaw before=" + newYaw +
//                                ", newYaw after=" + newYaw +
//                                ", clipSign=" + Math.copySign(1f, rawDiff)
//                ));
//            }
//
//
//            MODULE_MANAGER.getStorage().getByClass(Debug.class).debugDelta(Text.of(
//                    "Final newYaw=" + newYaw
//            ));
//        }

        stateHandler.setRotationYaw(newYaw);
        stateHandler.setRotationPitch(newPitch);
    }

    private void interpolateRenderRotation() {
        float currentRenderYaw = stateHandler.getRenderYaw();
        float currentRenderPitch = stateHandler.getRenderPitch();

        float targetYaw = stateHandler.getRotationYaw();
        float targetPitch = stateHandler.getRotationPitch();

        float factor = 0.7f;

        float newRenderYaw = lerpAngle(currentRenderYaw, targetYaw, factor);
        float newRenderPitch = lerp(currentRenderPitch, targetPitch, factor);

        stateHandler.setRenderYaw(newRenderYaw);
        stateHandler.setRenderPitch(newRenderPitch);
    }

    private float lerpAngle(float start, float end, float factor) {
        float delta = wrapDegrees(end - start);
        return start + delta * factor;
    }

    private void idleReset() {
        requestHandler.clearLastActiveId();
        stateHandler.setRotationYaw(stateHandler.getRealYaw());
        stateHandler.setRotationPitch(stateHandler.getRealPitch());
        currentYawSpeed = 0f;
        currentPitchSpeed = 0f;
    }

    private void updateHeld(KeyBinding bind, int key, int scancode, int action, boolean mouse, java.util.function.Consumer<Boolean> setter) {
        if (!canMove())
            return;

        if (!mouse && !bind.matchesKey(key, scancode)) return;
        if (mouse && !bind.matchesMouse(key)) return;

        setter.accept(action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT);
        if (action == GLFW.GLFW_RELEASE) {
            setter.accept(false);
        }
    }

    private boolean canMove() {
        if (MODULE_MANAGER.getStorage().getByClass(FreecamModule.class).isEnabled()) return false;
        
        if (MC.currentScreen == null) return true;

        if (MC.currentScreen != null && !MODULE_MANAGER.getStorage().getByClass(GuiMoveModule.class).isEnabled())
            return false;

        if (MC.currentScreen instanceof ChatScreen
                || MC.currentScreen instanceof SignEditScreen
                || MC.currentScreen instanceof AnvilScreen
                || MC.currentScreen instanceof AbstractCommandBlockScreen
                || MC.currentScreen instanceof StructureBlockScreen
                || MC.currentScreen instanceof CreativeInventoryScreen) {
            return false;
        }

        return true;
    }

    private float yawDifference(float targetYaw, float currentYaw) {
        float diff = (targetYaw - currentYaw) % 360f;
        if (diff >= 180f) diff -= 360f;
        if (diff < -180f) diff += 360f;
        return diff;
    }

    private float alignYaw(float playerYaw, float currentYaw) {
        int wraps = Math.round((currentYaw - playerYaw) / 360f);
        return playerYaw + wraps * 360f;
    }

    private float lerp(float from, float to, float factor) {
        return from + (to - from) * factor;
    }

    public boolean isRotating() {
        return requestHandler.getActiveRequest() != null || returning;
    }
}
