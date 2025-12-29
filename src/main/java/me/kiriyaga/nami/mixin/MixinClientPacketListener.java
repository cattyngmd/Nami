package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.ChatMessageEvent;
import me.kiriyaga.nami.event.impl.ChunkDataEvent;
import me.kiriyaga.nami.event.impl.WorldTimeUpdateEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Shadow
    private ClientLevel level;

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    public void onSendChatMessage(String message, CallbackInfo ci) {
        ChatMessageEvent event = new ChatMessageEvent(message);
        EVENT_MANAGER.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleLevelChunkWithLight", at = @At("TAIL"))
    private void onChunkData(ClientboundLevelChunkWithLightPacket packet, CallbackInfo info) {
        if (level == null) return;

        LevelChunk chunk = level.getChunk(packet.getX(), packet.getZ());
        if (chunk == null || chunk.isEmpty()) return;

        EVENT_MANAGER.post(new ChunkDataEvent(chunk));
    }

    @Inject(method = "handleSetTime", at = @At("HEAD"), cancellable = true)
    private void onWorldTimeUpdate(ClientboundSetTimePacket worldTimeUpdateS2CPacket, CallbackInfo ci) {
        WorldTimeUpdateEvent event = new WorldTimeUpdateEvent();
        EVENT_MANAGER.post(event);

        if (event.isCancelled()) ci.cancel();
    }
}
