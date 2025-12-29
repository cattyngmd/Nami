package me.kiriyaga.nami.mixin;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundInteractPacket.class)
public interface DuckServerboundInteractPacket {
    @Accessor("action")
    ServerboundInteractPacket.Action getTypeHandler();
}
