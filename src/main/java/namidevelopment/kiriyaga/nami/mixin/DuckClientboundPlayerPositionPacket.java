package namidevelopment.kiriyaga.nami.mixin;

import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientboundPlayerPositionPacket.class)
public abstract class DuckClientboundPlayerPositionPacket {

    @Shadow
    private PositionMoveRotation comp_3228;

    @Unique
    public void setEntityPosition(PositionMoveRotation pos) {
        this.comp_3228 = pos;
    }
}
