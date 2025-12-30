package me.kiriyaga.nami.feature.module.impl.visuals.blocksearch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.Set;

public class ChunkSnapshot {

    public final ChunkPos pos;
    public final Set<BlockPos> blocks;

    public ChunkSnapshot(ChunkPos pos, Set<BlockPos> blocks) {
        this.pos = pos;
        this.blocks = blocks;
    }

    public long getKey() {
        return ChunkPos.asLong(pos.x, pos.z);
    }
}
