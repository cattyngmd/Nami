package namidevelopment.kiriyaga.nami.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundMovePlayerPacket.class)
public interface DuckServerboundMovePlayerPacket {

    @Accessor("onGround")
    void setOnGround(boolean value);
}
