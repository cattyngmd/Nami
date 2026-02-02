package namidevelopment.kiriyaga.nami.event.impl;

import namidevelopment.kiriyaga.nami.event.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class StartBreakingBlockEvent extends Event {
    public BlockPos blockPos;
    public Direction direction;

    public StartBreakingBlockEvent(BlockPos blockPos, Direction direction) {
        this.blockPos = blockPos;
        this.direction = direction;
    }
}
