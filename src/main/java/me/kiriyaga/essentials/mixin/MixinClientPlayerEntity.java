package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.event.impl.PostTickEvent;
import me.kiriyaga.essentials.event.impl.PreTickEvent;
import me.kiriyaga.essentials.feature.module.impl.render.FreecamModule;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.kiriyaga.essentials.Essentials.*;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Shadow private float lastYawClient;
    @Shadow private float lastPitchClient;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickHookPre(CallbackInfo ci) {

        EVENT_MANAGER.post(new PreTickEvent());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickHookPost(CallbackInfo ci) {

        EVENT_MANAGER.post(new PostTickEvent());
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void spoofLastYawPitch(CallbackInfo ci) {
        if (ROTATION_MANAGER.isRotating()) {
            this.lastYawClient += (float)(Math.random() * 10 + 10);
            this.lastPitchClient += (float)(Math.random() * 10 + 10);
        }
    }
}
