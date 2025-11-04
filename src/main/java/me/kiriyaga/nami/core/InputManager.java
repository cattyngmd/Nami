package me.kiriyaga.nami.core;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.KeyInputEvent;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.impl.movement.GuiMoveModule;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.PlayerInput;
import org.lwjgl.glfw.GLFW;

import static me.kiriyaga.nami.Nami.*;

public class InputManager {

    private boolean forward, backward, left, right, jumping, sneaking, sprinting;
    private boolean forwardPressed, backPressed, leftPressed, rightPressed;
    private boolean frozen = false;
    private int freezeTicks = 0;
    private boolean savedForward, savedBack, savedLeft, savedRight;
    private boolean savedJump, savedSneak, savedSprint;

    public void init() {
        EVENT_MANAGER.register(this);
        LOGGER.info("Input Manager loaded");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyInput(KeyInputEvent event) {
        int key = event.key;
        int action = event.action;
        int scancode = event.scancode;

        if (!canMove()) return;

        updateHeld(MC.options.forwardKey, key, scancode, action, v -> forwardPressed = v);
        updateHeld(MC.options.leftKey,    key, scancode, action, v -> leftPressed = v);
        updateHeld(MC.options.backKey,    key, scancode, action, v -> backPressed = v);
        updateHeld(MC.options.rightKey,   key, scancode, action, v -> rightPressed = v);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof PlayerInputC2SPacket packet) {
            PlayerInput input = packet.comp_3139();

            this.forward = input.comp_3159();
            this.backward = input.comp_3160();
            this.left = input.comp_3161();
            this.right = input.comp_3162();
            this.jumping = input.comp_3163();
            this.sneaking = input.sneak();
            this.sprinting = input.comp_3165();
        } else if (event.getPacket() instanceof VehicleMoveC2SPacket) {
            // TODO: finish this
        } else if (event.getPacket() instanceof PlayerMoveC2SPacket) {
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreTick(PreTickEvent event) {
        if (freezeTicks > 0) {
            freezeTicks--;

            if (freezeTicks == 1) {
                disableAllKeys();
            } else if (freezeTicks == 0) {
                restoreKeys();
                frozen = false;
            }
        }
    }

    public void freezeInputNow() {
        if (frozen) return;
        frozen = true;
        freezeTicks = 1;

        saveKeys();
        disableAllKeys();
    }

    public boolean isFrozen() {
        return frozen;
    }

    public int getFrozenTicks() {
        return freezeTicks;
    }

    public boolean isForwardPressed() { return forwardPressed; }
    public boolean isBackPressed() { return backPressed; }
    public boolean isLeftPressed() { return leftPressed; }
    public boolean isRightPressed() { return rightPressed; }

    public boolean hasAnyInput() {
        return forward || backward || left || right || jumping || sneaking || sprinting;
    }

    public boolean isMoving() {
        return forward || backward || left || right;
    }

    private void saveKeys() {
        GameOptions opt = MC.options;
        savedForward = opt.forwardKey.isPressed();
        savedBack = opt.backKey.isPressed();
        savedLeft = opt.leftKey.isPressed();
        savedRight = opt.rightKey.isPressed();
        savedJump = opt.jumpKey.isPressed();
        savedSneak = opt.sneakKey.isPressed();
        savedSprint = opt.sprintKey.isPressed();
    }

    private void disableAllKeys() {
        GameOptions opt = MC.options;
        setPressed(opt.forwardKey, false);
        setPressed(opt.backKey, false);
        setPressed(opt.leftKey, false);
        setPressed(opt.rightKey, false);
        setPressed(opt.jumpKey, false);
        setPressed(opt.sneakKey, false);
        setPressed(opt.sprintKey, false);
    }

    private void restoreKeys() {
        GameOptions opt = MC.options;
        setPressed(opt.forwardKey, savedForward);
        setPressed(opt.backKey, savedBack);
        setPressed(opt.leftKey, savedLeft);
        setPressed(opt.rightKey, savedRight);
        setPressed(opt.jumpKey, savedJump);
        setPressed(opt.sneakKey, savedSneak);
        setPressed(opt.sprintKey, savedSprint);
    }

    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
    }

    private void updateHeld(KeyBinding bind, int key, int scancode, int action, java.util.function.Consumer<Boolean> setter) {
        if (!bind.matchesKey(key, scancode)) return;
        setter.accept(action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT);
        if (action == GLFW.GLFW_RELEASE) setter.accept(false);
    }

    private boolean canMove() {
        if (MODULE_MANAGER.getStorage().getByClass(FreecamModule.class).isEnabled()) return false;
        if (MC.currentScreen == null) return true;
        if (MC.currentScreen != null && !MODULE_MANAGER.getStorage().getByClass(GuiMoveModule.class).isEnabled()) return false;
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
}
