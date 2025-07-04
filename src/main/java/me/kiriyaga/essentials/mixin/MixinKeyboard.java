package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.event.impl.KeyInputEvent;
import me.kiriyaga.essentials.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.essentials.feature.module.impl.render.FreecamModule;
import me.kiriyaga.essentials.setting.impl.KeyBindSetting;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.essentials.Essentials.*;

@Mixin(Keyboard.class)
public abstract class MixinKeyboard {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
        public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (key == GLFW.GLFW_KEY_F3) return;

        KeyInputEvent event = new KeyInputEvent(key, scancode, action, modifiers);
        EVENT_MANAGER.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKeyClickgui(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (MINECRAFT == null) return;
        if (action != GLFW.GLFW_PRESS) return;

        ClickGuiModule clickGui = MODULE_MANAGER.getModule(ClickGuiModule.class);
        KeyBindSetting bind = clickGui.getKeyBind();

        if (bind.get() == key) {
            if (MINECRAFT.world == null) {
                clickGui.toggle();
            }
        }
    }
}
