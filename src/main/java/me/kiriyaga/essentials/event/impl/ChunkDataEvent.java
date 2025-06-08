package me.kiriyaga.essentials.event.impl;

import me.kiriyaga.essentials.event.Event;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkDataEvent extends Event {
    private final WorldChunk chunk;

    public ChunkDataEvent(WorldChunk chunk) {
        this.chunk = chunk;
    }

    public WorldChunk getChunk() {
        return chunk;
    }
}
