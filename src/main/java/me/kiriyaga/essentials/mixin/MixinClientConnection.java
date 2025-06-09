package me.kiriyaga.essentials.mixin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.kiriyaga.essentials.event.impl.PacketReceiveEvent;
import me.kiriyaga.essentials.event.impl.PacketSendEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.essentials.Essentials.EVENT_MANAGER;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Shadow private Channel channel;
    @Shadow @Final private NetworkSide side;

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void onPacketReceive(ChannelHandlerContext chc, Packet<?> packet, CallbackInfo ci) {
        if (this.channel.isOpen() && packet != null) {
            PacketReceiveEvent event = new PacketReceiveEvent(packet);
            EVENT_MANAGER.post(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "sendImmediately", at = @At("HEAD"), cancellable = true)
    private void onPacketSend(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if (this.side != NetworkSide.CLIENTBOUND) return;

        PacketSendEvent event = new PacketSendEvent(packet);
        EVENT_MANAGER.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
