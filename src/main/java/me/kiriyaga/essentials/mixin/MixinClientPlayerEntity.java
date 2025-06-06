package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.Essentials;
import me.kiriyaga.essentials.event.events.UpdateEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.essentials.Essentials.EVENT_MANAGER;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickHook(CallbackInfo ci) {
        var mc = net.minecraft.client.MinecraftClient.getInstance();

        EVENT_MANAGER.post(new UpdateEvent());
    }
}
