package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.feature.module.impl.render.FreecamModule;
import me.kiriyaga.essentials.feature.module.impl.world.AutoRespawnModule;
import me.kiriyaga.essentials.manager.RotationManager;
import me.kiriyaga.essentials.mixininterface.IMouseDeltaAccessor;
import me.kiriyaga.essentials.setting.impl.KeyBindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.kiriyaga.essentials.feature.module.Module;

import static me.kiriyaga.essentials.Essentials.*;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Shadow @Final public Mouse mouse;
    private double lastMouseX = -1;
    private double lastMouseY = -1;

    @Inject(method = "handleInputEvents", at = @At("TAIL"))
    private void onHandleInputEvents_TAIL(CallbackInfo ci) {
        if (MINECRAFT == null || MINECRAFT.mouse == null) return;

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

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void setScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof DeathScreen && MINECRAFT.player != null && MODULE_MANAGER.getModule(AutoRespawnModule.class).isEnabled()) {

            if (MODULE_MANAGER.getModule(AutoRespawnModule.class).sendCords.get()){
                Vec3d pos = MINECRAFT.player.getPos();
                String coords = String.format("Death coordinates: X: %d Y: %d Z: %d",
                        Math.round(pos.x), Math.round(pos.y), Math.round(pos.z));

                CHAT_MANAGER.sendPersistent(AutoRespawnModule.class.getName(), "Death coordinates: §7" + coords);
            }

            MINECRAFT.player.requestRespawn();
            info.cancel();
        }
    }
}
