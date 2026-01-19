package me.kiriyaga.nami.mixin;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import me.kiriyaga.nami.feature.module.impl.miscellaneous.NoPacketKick;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Invoker;

import static me.kiriyaga.nami.Nami.*;

@Mixin(Connection.class)
public abstract class MixinConnection {

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void onPacketReceive(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        PacketReceiveEvent event = new PacketReceiveEvent(packet);
        EVENT_MANAGER.post(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }

        if (event.getPacket() != packet) {
            ci.cancel();
            ctx.fireChannelRead(event.getPacket());
            return;
        }

    }

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void onPacketSend(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        PacketSendEvent event = new PacketSendEvent(packet);
        EVENT_MANAGER.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaught(ChannelHandlerContext context, Throwable exception, CallbackInfo ci) {
        NoPacketKick module = MODULE_MANAGER.getStorage().getByClass(NoPacketKick.class);
        if (module != null && module.isEnabled()) {
            LOGGER.error("Packet exception caught: ", exception);
            ci.cancel();
        }
    }
}