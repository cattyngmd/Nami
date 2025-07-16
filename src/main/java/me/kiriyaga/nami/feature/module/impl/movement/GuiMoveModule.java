package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.KeyInputEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.render.FreecamModule;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class GuiMoveModule extends Module {

    private boolean forwardHeld = false;
    private boolean backHeld = false;
    private boolean leftHeld = false;
    private boolean rightHeld = false;
    private boolean jumpHeld = false;
    private boolean sneakHeld = false;
    private boolean sprintHeld = false;

    public GuiMoveModule() {
        super("gui move", "Allows movement in most GUIs.", Category.movement);
    }

    @Override
    public void onDisable() {
        forwardHeld = false;
        backHeld = false;
        leftHeld = false;
        rightHeld = false;
        jumpHeld = false;
        sneakHeld = false;
        sprintHeld = false;
        setKeysPressed(false);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (!canMove()) return;


        updateHeld(MC.options.forwardKey, event.key, event.action, false, v -> forwardHeld = v);
        updateHeld(MC.options.backKey, event.key, event.action, false, v -> backHeld = v);
        updateHeld(MC.options.leftKey, event.key, event.action, false, v -> leftHeld = v);
        updateHeld(MC.options.rightKey, event.key, event.action, false, v -> rightHeld = v);
        updateHeld(MC.options.jumpKey, event.key, event.action, false, v -> jumpHeld = v);
        updateHeld(MC.options.sneakKey, event.key, event.action, false, v -> sneakHeld = v);
        updateHeld(MC.options.sprintKey, event.key, event.action, false, v -> sprintHeld = v);
    }

    private void updateHeld(KeyBinding bind, int key, int action, boolean mouse, java.util.function.Consumer<Boolean> setter) {
        if (!mouse && !bind.matchesKey(key, 0)) return;
        if (mouse && !bind.matchesMouse(key)) return;

        setter.accept(action == GLFW.GLFW_PRESS);
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent event) {
        if (!canMove() || MODULE_MANAGER.getModule(FreecamModule.class).isEnabled()) return;

        if (MC.currentScreen == null) {
            forwardHeld = backHeld = leftHeld = rightHeld = jumpHeld = sneakHeld = sprintHeld = false;
            setKeysPressed(false);
            return;
        }

        updateKeyWithHold(MC.options.forwardKey, forwardHeld);
        updateKeyWithHold(MC.options.backKey, backHeld);
        updateKeyWithHold(MC.options.leftKey, leftHeld);
        updateKeyWithHold(MC.options.rightKey, rightHeld);
        updateKeyWithHold(MC.options.jumpKey, jumpHeld);
        updateKeyWithHold(MC.options.sneakKey, sneakHeld);
        updateKeyWithHold(MC.options.sprintKey, sprintHeld);
    }

    private void updateKeyWithHold(KeyBinding bind, boolean held) {
        InputUtil.Key boundKey = ((KeyBindingAccessor) bind).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), keyCode);
        bind.setPressed(physicallyPressed || held);
    }

    private boolean canMove() {
        if (MC.currentScreen == null) return false;
        return !(MC.currentScreen instanceof ChatScreen
                || MC.currentScreen instanceof SignEditScreen
                || MC.currentScreen instanceof AnvilScreen
                || MC.currentScreen instanceof AbstractCommandBlockScreen
                || MC.currentScreen instanceof StructureBlockScreen
                || MC.currentScreen instanceof CreativeInventoryScreen
        );
    }

    private void setKeysPressed(boolean pressed) {
        MC.options.forwardKey.setPressed(pressed);
        MC.options.backKey.setPressed(pressed);
        MC.options.leftKey.setPressed(pressed);
        MC.options.rightKey.setPressed(pressed);
        MC.options.jumpKey.setPressed(pressed);
        MC.options.sneakKey.setPressed(pressed);
        MC.options.sprintKey.setPressed(pressed);
    }
}
