package me.kiriyaga.nami.core.breaking.model;

import net.minecraft.util.math.BlockPos;

public class BreakTarget {
    private final BlockPos pos;
    private final long timestamp;

    public BreakTarget(BlockPos pos) {
        this.pos = pos;
        this.timestamp = System.currentTimeMillis();
    }

    public BlockPos getPos() {
        return pos;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
