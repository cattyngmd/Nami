package namidevelopment.kiriyaga.api.mixin;

import namidevelopment.kiriyaga.api.core.macro.model.Macro;
import namidevelopment.kiriyaga.api.model.setting.KeyBindSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import namidevelopment.kiriyaga.api.model.feature.Feature;

import static namidevelopment.kiriyaga.api.NamiApi.*;
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow @Nullable public LocalPlayer player;
    @Shadow public ClientLevel level;


    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void onHandleInputEvents_TAIL(CallbackInfo ci) {
        if (API_MC == null || API_MC.mouseHandler == null || API_MC.screen != null) return;

        for (Feature Feature : FEATURE_SERVICE.getStorage().getAll()) {
            if (Feature == null) continue;
            KeyBindSetting bind = Feature.getKeyBind();
            if (bind == null) continue;

            if (bind.get() != KeyBindSetting.KEY_NONE) {
                boolean currentlyPressed = bind.isPressed();

                if (bind.isHoldMode()) {
                    if (currentlyPressed && !Feature.isEnabled()) {
                        Feature.setEnabled(true);
                    } else if (!currentlyPressed && Feature.isEnabled()) {
                        Feature.setEnabled(false);
                    }
                } else {
                    if (currentlyPressed && !bind.wasPressedLastTick()) {
                        Feature.toggle();
                    }
                }

                bind.setWasPressedLastTick(currentlyPressed);
            }
        }

        for (Macro macro : MACRO_SERVICE.getAll()) {
            int keyCode = macro.getKeyCode();
            boolean currentlyPressed = MACRO_SERVICE.isKeyPressed(keyCode);
            boolean wasPressed = MACRO_SERVICE.wasKeyPressedLastTick(keyCode);

            if (currentlyPressed && !wasPressed) {
                if (API_MC.player != null) {
                    API_MC.player.connection.sendChat(macro.getMessage());
                }
            }

            MACRO_SERVICE.setKeyPressedLastTick(keyCode, currentlyPressed);
        }
    }
}