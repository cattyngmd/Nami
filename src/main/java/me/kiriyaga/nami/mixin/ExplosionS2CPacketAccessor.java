package me.kiriyaga.nami.mixin;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(ExplosionS2CPacket.class)
public interface ExplosionS2CPacketAccessor {
    @Accessor("playerKnockback")
    Optional<Vec3d> getPlayerKnockback();

    @Accessor("playerKnockback")
    @Mutable
    void setPlayerKnockback(Optional<Vec3d> playerKnockback);
}
