package me.kiriyaga.nami.feature.module.impl.visuals.blocksearch;

import net.minecraft.core.BlockPos;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class BlockSearch extends Thread {

    private final BlockingQueue<ChunkSnapshot> queue;
    private final ConcurrentMap<Long, Set<BlockPos>> resultMap;
    private volatile boolean running = true;

    public BlockSearch(
            BlockingQueue<ChunkSnapshot> queue,
            ConcurrentMap<Long, Set<BlockPos>> resultMap
    ) {
        this.queue = queue;
        this.resultMap = resultMap;
        setName("BlockSearchWorker");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                ChunkSnapshot snap = queue.take();

                if (snap.blocks.isEmpty()) {
                    resultMap.remove(snap.getKey());
                } else {
                    resultMap.put(snap.getKey(), snap.blocks);
                }

            } catch (InterruptedException ignored) {}
        }
    }

    public void stopWorker() {
        running = false;
        interrupt();
    }
}
