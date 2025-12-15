package me.kiriyaga.nami.mixin;

import net.minecraft.entity.EntityPosition;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerPositionLookS2CPacket.class)
public abstract class PlayerPositionLookS2CPacketAcessor {

    @Shadow
    private EntityPosition comp_3228;

    @Unique
    public void setEntityPosition(EntityPosition pos) {
        this.comp_3228 = pos;
    }
}
