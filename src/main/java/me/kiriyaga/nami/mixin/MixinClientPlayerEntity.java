package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.kiriyaga.nami.Nami.*;

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
