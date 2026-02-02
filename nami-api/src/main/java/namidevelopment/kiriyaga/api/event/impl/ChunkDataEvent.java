package namidevelopment.kiriyaga.api.event.impl;

import namidevelopment.kiriyaga.api.event.Event;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkDataEvent extends Event {
    private final LevelChunk chunk;

    public ChunkDataEvent(LevelChunk chunk) {
        this.chunk = chunk;
    }

    public LevelChunk getChunk() {
        return chunk;
    }
}
