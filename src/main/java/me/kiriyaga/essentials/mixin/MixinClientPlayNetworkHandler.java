package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.Essentials;
import me.kiriyaga.essentials.event.impl.ChatMessageEvent;
import me.kiriyaga.essentials.event.impl.ChunkDataEvent;
import me.kiriyaga.essentials.feature.module.impl.movement.VelocityModule;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.essentials.Essentials.*;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Shadow
    private ClientWorld world;


    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void onSendChatMessage(String message, CallbackInfo ci) {
        ChatMessageEvent event = new ChatMessageEvent(message);
        EVENT_MANAGER.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void onChunkData(ChunkDataS2CPacket packet, CallbackInfo info) {
        if (world == null) return;

        WorldChunk chunk = world.getChunk(packet.getChunkX(), packet.getChunkZ());
        if (chunk == null || chunk.isEmpty()) return;

        EVENT_MANAGER.post(new ChunkDataEvent(chunk));
    }

    private VelocityModule getVelocityModule() {
        return (VelocityModule) MODULE_MANAGER.getModule(VelocityModule.class);
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
            shift = At.Shift.AFTER),
            cancellable = true)
    private void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        VelocityModule velocityModule = getVelocityModule();
        if (velocityModule == null || !velocityModule.isEnabled() || !velocityModule.packetDecline.get()) return;

        if (packet.getEntityId() == MINECRAFT.player.getId()) {
            ci.cancel();
        }
    }
}
