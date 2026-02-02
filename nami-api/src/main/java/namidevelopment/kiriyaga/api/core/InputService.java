package namidevelopment.kiriyaga.api.core;

import namidevelopment.kiriyaga.api.event.EventPriority;
import namidevelopment.kiriyaga.api.event.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.impl.KeyInputEvent;
import namidevelopment.kiriyaga.api.event.impl.PacketSendEvent;
import namidevelopment.kiriyaga.api.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.util.InputCache;
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

import static namidevelopment.kiriyaga.api.NamiApi.*;

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

        updateHeld(API_MC.options.keyUp, key, scancode, action, v -> forwardPressed = v);
        updateHeld(API_MC.options.keyLeft,    key, scancode, action, v -> leftPressed = v);
        updateHeld(API_MC.options.keyDown,    key, scancode, action, v -> backPressed = v);
        updateHeld(API_MC.options.keyRight,   key, scancode, action, v -> rightPressed = v);
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
        Options opt = API_MC.options;
        savedForward = opt.keyUp.isDown();
        savedBack = opt.keyDown.isDown();
        savedLeft = opt.keyLeft.isDown();
        savedRight = opt.keyRight.isDown();
        savedJump = opt.keyJump.isDown();
        savedSneak = opt.keyShift.isDown();
        savedSprint = opt.keySprint.isDown();
    }

    private void disableAllKeys() {
        Options opt = API_MC.options;
        setPressed(opt.keyUp, false);
        setPressed(opt.keyDown, false);
        setPressed(opt.keyLeft, false);
        setPressed(opt.keyRight, false);
        setPressed(opt.keyJump, false);
        setPressed(opt.keyShift, false);
        setPressed(opt.keySprint, false);
    }

    private void restoreKeys() {
        Options opt = API_MC.options;
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
        float realYaw = API_MC.player.getYRot();

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
        if (FEATURE_SERVICE.getStorage().getByName("Freecam")!= null && FEATURE_SERVICE.getStorage().getByName("Freecam").isEnabled()) return false;
        if (API_MC.screen == null) return true;
        if (API_MC.screen != null && FEATURE_SERVICE.getStorage().getByName("GuiMove") != null && FEATURE_SERVICE.getStorage().getByName("GuiMove").isEnabled()) return false;
        if (API_MC.screen instanceof ChatScreen
                || API_MC.screen instanceof SignEditScreen
                || API_MC.screen instanceof AnvilScreen
                || API_MC.screen instanceof AbstractCommandBlockEditScreen
                || API_MC.screen instanceof StructureBlockEditScreen
                || API_MC.screen instanceof CreativeModeInventoryScreen) {
            return false;
        }
        return true;
    }
}
