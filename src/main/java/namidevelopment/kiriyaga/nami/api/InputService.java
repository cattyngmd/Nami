package namidevelopment.kiriyaga.nami.api;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.KeyInputEvent;
import namidevelopment.kiriyaga.nami.event.impl.PacketSendEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.impl.movement.GuiMoveFeature;
import namidevelopment.kiriyaga.nami.impl.feature.impl.visuals.FreecamFeature;
import namidevelopment.kiriyaga.nami.util.InputCache;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.Options;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.entity.player.Input;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import static namidevelopment.kiriyaga.nami.Nami.*; // TODO: 1.20.6 viafabric flags sprinting, since packet does not exists. The grim check, does not apply for input on theese versions, but do apply for sprinting

public class InputService {

    private boolean forward, backward, left, right, jumping, sneaking, sprinting;
    private boolean forwardPressed, backPressed, leftPressed, rightPressed;
    private boolean frozen = false;
    private int freezeTicks = 0;
    private boolean savedForward, savedBack, savedLeft, savedRight;
    private boolean savedJump, savedSneak, savedSprint;

    public void init() {
        EVENT_SERVICE.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyInput(KeyInputEvent event) {
        if (!canMove()) return;

        int key = event.key;
        int scancode = event.scancode;
        int action = event.action;

        updateHeld(MC.options.keyUp, key, scancode, action, v -> forwardPressed = v);
        updateHeld(MC.options.keyLeft,    key, scancode, action, v -> leftPressed = v);
        updateHeld(MC.options.keyDown,    key, scancode, action, v -> backPressed = v);
        updateHeld(MC.options.keyRight,   key, scancode, action, v -> rightPressed = v);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof ServerboundPlayerInputPacket packet) {
            Input input = packet.input();

            this.forward = input.forward();
            this.backward = input.backward();
            this.left = input.left();
            this.right = input.right();
            this.jumping = input.jump();
            this.sneaking = input.shift();
            this.sprinting = input.sprint();
        } else if (event.getPacket() instanceof ServerboundMoveVehiclePacket) {
            // TODO: finish this
        } else if (event.getPacket() instanceof ServerboundMovePlayerPacket) {
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreTick(PreTickEvent event) {
        if (!frozen) return;

        freezeTicks--;

        if (freezeTicks <= 0) {
            restoreKeys();
            frozen = false;
        } else {
            disableAllKeys();
        }
    }

    public void freezeInput(int i) {
        if (frozen) return;
        frozen = true;
        freezeTicks = i;

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
    public boolean isJumpPressed() { return jumping; }
    public boolean isShiftPressed() { return sneaking; }

    public boolean hasAnyInput() {
        return forward || backward || left || right || jumping || sneaking || sprinting;
    }

    public boolean isMoving() {
        return forward || backward || left || right;
    }

    private void saveKeys() {
        Options opt = MC.options;
        savedForward = opt.keyUp.isDown();
        savedBack = opt.keyDown.isDown();
        savedLeft = opt.keyLeft.isDown();
        savedRight = opt.keyRight.isDown();
        savedJump = opt.keyJump.isDown();
        savedSneak = opt.keyShift.isDown();
        savedSprint = opt.keySprint.isDown();
    }

    private void disableAllKeys() {
        Options opt = MC.options;
        setPressed(opt.keyUp, false);
        setPressed(opt.keyDown, false);
        setPressed(opt.keyLeft, false);
        setPressed(opt.keyRight, false);
        setPressed(opt.keyJump, false);
        setPressed(opt.keyShift, false);
        setPressed(opt.keySprint, false);
    }

    private void restoreKeys() {
        Options opt = MC.options;
        setPressed(opt.keyUp, savedForward);
        setPressed(opt.keyDown, savedBack);
        setPressed(opt.keyLeft, savedLeft);
        setPressed(opt.keyRight, savedRight);
        setPressed(opt.keyJump, savedJump);
        setPressed(opt.keyShift, savedSneak);
        setPressed(opt.keySprint, savedSprint);
    }

    private void setPressed(KeyMapping key, boolean pressed) {
        key.setDown(pressed);
    }

    private void updateHeld(KeyMapping bind, int key, int scancode, int action, java.util.function.Consumer<Boolean> setter) {
        KeyEvent input = new KeyEvent(key, scancode, 0);
        if (!bind.matches(input)) return;
        boolean pressed = action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT;
        setter.accept(pressed);
        if (action == GLFW.GLFW_RELEASE) {
            setter.accept(false);
        }
    }


    public float getDirection() {
        float realYaw = MC.player.getYRot();

        boolean forward = InputCache.forward;
        boolean back = InputCache.back;
        boolean left = InputCache.left;
        boolean right = InputCache.right;

        int inputX = (right ? 1 : 0) - (left ? 1 : 0);
        int inputZ = (forward ? 1 : 0) - (back ? 1 : 0);

        if (inputX == 0 && inputZ == 0) return realYaw;

        if (inputZ > 0) return realYaw;

        if (inputZ < 0) return Mth.wrapDegrees(realYaw + 180);

        if (inputX != 0 && inputZ == 0) return Mth.wrapDegrees(realYaw + (inputX > 0 ? 90 : -90));

        if (inputZ > 0 && inputX != 0) return realYaw;

        if (inputZ < 0 && inputX != 0) return Mth.wrapDegrees(realYaw + 180);

        return realYaw;
    }

    private boolean canMove() {
        if (FEATURE_SERVICE.getStorage().getByClass(FreecamFeature.class).isEnabled()) return false;
        if (MC.screen == null) return true;
        if (MC.screen != null && !FEATURE_SERVICE.getStorage().getByClass(GuiMoveFeature.class).isEnabled()) return false;
        if (MC.screen instanceof ChatScreen
                || MC.screen instanceof SignEditScreen
                || MC.screen instanceof AnvilScreen
                || MC.screen instanceof AbstractCommandBlockEditScreen
                || MC.screen instanceof StructureBlockEditScreen
                || MC.screen instanceof CreativeModeInventoryScreen) {
            return false;
        }
        return true;
    }
}
