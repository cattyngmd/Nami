package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.nami.event.impl.*;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static namidevelopment.kiriyaga.nami.Nami.*;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Shadow
    private ClientLevel level;

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    public void onSendChatMessage(String message, CallbackInfo ci) {
        ChatMessageEvent event = new ChatMessageEvent(message);
        EVENT_SERVICE.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleAddEntity", at = @At("TAIL"))
    private void onHandleAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        AddEntityEvent event = new AddEntityEvent(packet);
        EVENT_SERVICE.post(event);
    }

    @Inject(method = "handleRemoveEntities", at = @At("TAIL"))
    private void onHandleRemoveEntities(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci) {
        RemoveEntityEvent event = new RemoveEntityEvent(packet);
        EVENT_SERVICE.post(event);
    }

    @Inject(method = "handleLevelChunkWithLight", at = @At("TAIL"))
    private void onChunkData(ClientboundLevelChunkWithLightPacket packet, CallbackInfo info) {
        if (level == null) return;

        LevelChunk chunk = level.getChunk(packet.getX(), packet.getZ());
        if (chunk == null || chunk.isEmpty()) return;

        EVENT_SERVICE.post(new ChunkDataEvent(chunk));
    }

    @Inject(method = "handleSetTime", at = @At("HEAD"), cancellable = true)
    private void onWorldTimeUpdate(ClientboundSetTimePacket worldTimeUpdateS2CPacket, CallbackInfo ci) {
        WorldTimeUpdateEvent event = new WorldTimeUpdateEvent();
        EVENT_SERVICE.post(event);

        if (event.isCancelled()) ci.cancel();
    }
}
