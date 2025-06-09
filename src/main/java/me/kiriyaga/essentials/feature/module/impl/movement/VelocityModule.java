package me.kiriyaga.essentials.feature.module.impl.movement;

import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PacketReceiveEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.mixin.EntityVelocityUpdateS2CPacketAccessor;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class VelocityModule extends Module {

    public final BoolSetting entityPush = addSetting(new BoolSetting("Entity", true));
    public final DoubleSetting entityPushAmount = addSetting(new DoubleSetting("Entity Push Factor", 0, 0, 1));
    public final BoolSetting packetDecline = addSetting(new BoolSetting("Packet decline", false));

    public VelocityModule() {
        super("Velocity", "Modifies the amount of velocity.", Category.MOVEMENT);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isEnabled()) return;
        if (!entityPush.get()) return;

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
            if (packet.getEntityId() == MINECRAFT.player.getId()) {
                double newVelX = packet.getVelocityX() * entityPushAmount.get();
                double newVelY = packet.getVelocityY() * entityPushAmount.get();
                double newVelZ = packet.getVelocityZ() * entityPushAmount.get();

                EntityVelocityUpdateS2CPacketAccessor accessor = (EntityVelocityUpdateS2CPacketAccessor) packet;
                accessor.setX((int) newVelX);
                accessor.setY((int) newVelY);
                accessor.setZ((int) newVelZ);
            }
        }
    }
}
