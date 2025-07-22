package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.PlayerPositionAccessor;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class NoRotateModule extends Module {

    public NoRotateModule() {
        super("no rotate", "Prevents you from receiving forced server rotate packets.", ModuleCategory.of("movement"));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void onPacketRecieve(PacketReceiveEvent ev){
        if (ev.getPacket() instanceof PlayerPositionLookS2CPacket packet) {

            ((PlayerPositionAccessor) (Object) packet.change()).setYaw(MC.player.getYaw());
            ((PlayerPositionAccessor) (Object) packet.change()).setPitch(MC.player.getPitch());

            packet.relatives().remove(PositionFlag.X_ROT);
            packet.relatives().remove(PositionFlag.Y_ROT);
        }
    }
}
