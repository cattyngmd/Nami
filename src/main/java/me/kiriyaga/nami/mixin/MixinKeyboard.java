package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.KeyInputEvent;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.setting.impl.KeyBindSetting;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(Keyboard.class)
public abstract class MixinKeyboard {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
        public void onKey(long l, int i, KeyInput keyInput, CallbackInfo ci) {
        if (keyInput.getKeycode() == GLFW.GLFW_KEY_F3) return;

        KeyInputEvent event = new KeyInputEvent(keyInput.getKeycode(), keyInput.comp_4796(), i, keyInput.comp_4797());
        EVENT_MANAGER.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKeyClickgui(long l, int i, KeyInput keyInput, CallbackInfo ci) {
        if (MC == null) return;
        if (i != GLFW.GLFW_PRESS) return;

        if (MODULE_MANAGER.getStorage() == null) return;

        ClickGuiModule clickGui = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
        if (clickGui == null) return;

        KeyBindSetting bind = clickGui.getKeyBind();
        if (bind == null) return;

        if (bind.get() == keyInput.getKeycode()) {
            Screen screen = MC.currentScreen;

            if (screen instanceof TitleScreen
                    || screen instanceof SelectWorldScreen
                    || screen instanceof MultiplayerScreen) {

                if (!(screen.getFocused() instanceof EditBox)) {
                    clickGui.toggle();
                }
            }
        }
    }

}
