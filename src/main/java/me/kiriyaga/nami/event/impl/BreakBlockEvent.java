package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;

public class BreakBlockEvent extends Event {
    private final BlockPos pos;


    public BreakBlockEvent(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }
}
