package namidevelopment.kiriyaga.api.event.impl;

import namidevelopment.kiriyaga.api.event.Event;
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
