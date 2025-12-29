package me.kiriyaga.nami.mixininterface;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;

public interface IPlayerInteractEntityC2SPacket {
    ServerboundInteractPacket.ActionType getType();
    Entity getEntity();
}
