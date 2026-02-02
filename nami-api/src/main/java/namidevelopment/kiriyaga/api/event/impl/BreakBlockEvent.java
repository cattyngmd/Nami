package namidevelopment.kiriyaga.api.event.impl;

import namidevelopment.kiriyaga.api.event.Event;
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
