package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.event.impl.PostTickEvent;
import me.kiriyaga.essentials.event.impl.PreTickEvent;
import me.kiriyaga.essentials.feature.module.impl.render.FreecamModule;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.kiriyaga.essentials.Essentials.EVENT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Shadow public PlayerInput input;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickHookPre(CallbackInfo ci) {

        EVENT_MANAGER.post(new PreTickEvent());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickHookPost(CallbackInfo ci) {

        EVENT_MANAGER.post(new PostTickEvent());
    }

    @Inject(method = "tickMovementInput", at = @At("HEAD"))
    private void onTickMovementInput(CallbackInfo ci) {
        FreecamModule module = MODULE_MANAGER.getModule(FreecamModule.class);
        if (module.isEnabled()) {
            this.input = new PlayerInput(false, false, false, false, false, false, false);
        }
    }
}
