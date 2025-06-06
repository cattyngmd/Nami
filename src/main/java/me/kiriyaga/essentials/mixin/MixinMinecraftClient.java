package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.setting.settings.KeyBindSetting;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.kiriyaga.essentials.feature.module.Module;

import static me.kiriyaga.essentials.Essentials.*;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "handleInputEvents", at = @At("TAIL"))
    private void onHandleInputEvents(CallbackInfo ci) {

        if (MINECRAFT.currentScreen != null) return;

        for (Module module : MODULE_MANAGER.getModules()) {
            KeyBindSetting bind = module.getKeyBind();
            if (bind.get() != KeyBindSetting.KEY_NONE) {
                boolean currentlyPressed = bind.isPressed();

                if (bind.isHoldMode()) {
                    if (currentlyPressed && !module.isEnabled()) {
                        module.setEnabled(true);
                    } else if (!currentlyPressed && module.isEnabled()) {
                        module.setEnabled(false);
                    }
                } else {
                    if (currentlyPressed && !bind.wasPressedLastTick()) {
                        module.toggle();
                    }
                }

                bind.setWasPressedLastTick(currentlyPressed);
            }
        }

    }
}
