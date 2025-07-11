package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.misc.AutoRespawnModule;
import me.kiriyaga.nami.feature.module.impl.world.FastPlaceModule;
import me.kiriyaga.nami.feature.module.impl.world.NoHitDelayModule;
import me.kiriyaga.nami.setting.impl.KeyBindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.kiriyaga.nami.feature.module.Module;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.*;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Shadow
    public int attackCooldown;
    @Shadow private int itemUseCooldown;

    private int holdTicks = 0;

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

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z", ordinal = 0, shift = At.Shift.BEFORE))
    private void doItemUse(CallbackInfo info) {
        FastPlaceModule fastPlace = MODULE_MANAGER.getModule(FastPlaceModule.class);

        if (holdTicks >= fastPlace.startDelay.get() && fastPlace.isEnabled()) {
            itemUseCooldown = fastPlace.delay.get();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        FastPlaceModule fastPlace = MODULE_MANAGER.getModule(FastPlaceModule.class);

        if (fastPlace.isEnabled() && MINECRAFT.options.useKey.isPressed()) {
            holdTicks++;
        } else {
            holdTicks = 0;
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void setScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof DeathScreen && MINECRAFT.player != null && MODULE_MANAGER.getModule(AutoRespawnModule.class).isEnabled()) {

            if (MODULE_MANAGER.getModule(AutoRespawnModule.class).sendCords.get()) {
                Vec3d pos = MINECRAFT.player.getPos();
                String coords = String.format("X: %d Y: %d Z: %d",
                        Math.round(pos.x), Math.round(pos.y), Math.round(pos.z));

                CHAT_MANAGER.sendPersistent(AutoRespawnModule.class.getName(), "Death coordinates: §7" + coords);
            }

            MINECRAFT.player.requestRespawn();
            info.cancel();
        }
    }

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void doAttack(CallbackInfoReturnable<Boolean> info) {
        if (MODULE_MANAGER.getModule(NoHitDelayModule.class).isEnabled())
            attackCooldown = 0;
    }
}
