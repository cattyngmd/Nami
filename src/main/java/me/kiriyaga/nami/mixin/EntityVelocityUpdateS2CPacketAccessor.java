package me.kiriyaga.nami.mixin;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface EntityVelocityUpdateS2CPacketAccessor {

    @Accessor("velocity")
    Vec3d getVelocity();

    @Invoker("<init>")
    static EntityVelocityUpdateS2CPacket create(int entityId, Vec3d velocity) {
        throw new UnsupportedOperationException();
    }
}
