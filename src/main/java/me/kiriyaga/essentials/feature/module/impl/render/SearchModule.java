package me.kiriyaga.essentials.feature.module.impl.render;

import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.ChunkDataEvent;
import me.kiriyaga.essentials.event.impl.Render3DEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.util.BlockUtil;
import me.kiriyaga.essentials.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class SearchModule extends Module {

    private final BoolSetting storages = addSetting(new BoolSetting("Storages", true));
    private final BoolSetting nonVanilla = addSetting(new BoolSetting("Non-Vanilla", false));
    private final BoolSetting notifier = addSetting(new BoolSetting("Notifier", false));
    private final BoolSetting notAtSpawn = addSetting(new BoolSetting("Not At Spawn", false));
    private final DoubleSetting lineWidth = addSetting(new DoubleSetting("Line Width", 1.5, 0.5, 2.5));
    private final BoolSetting filled = addSetting(new BoolSetting("Filled", true));

    private ExecutorService workerThread = Executors.newSingleThreadExecutor();

    private final ConcurrentMap<Long, Set<BlockPos>> chunkBlocks = new ConcurrentHashMap<>();

    private Set<Identifier> candidateBlockIds = new HashSet<>();

    public SearchModule() {
        super("Search", "Search certain blocks on loaded chunks.", Category.RENDER, "srcj", "blockesp", "serch", "ыуфкср");
    }

    private void updateCandidateBlocks() {
        Set<Block> blocks = new HashSet<>();
        if (storages.get()) blocks.addAll(BlockUtil.getStorage());
        if (nonVanilla.get()) blocks.addAll(BlockUtil.getNonVanillaGeneratedBlocks());

        candidateBlockIds = blocks.stream()
                .map(Registries.BLOCK::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

    }


    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent event) {
        if (MINECRAFT.world == null
                || MINECRAFT.player == null) return;

        if (notAtSpawn.get()) {
            BlockPos pos = MINECRAFT.player.getBlockPos();
            if (Math.abs(pos.getX()) + Math.abs(pos.getZ()) < 5000)
                return;
        }

        updateCandidateBlocks();

        Chunk chunk = event.getChunk();
        BlockPos chunkStart = chunk.getPos().getStartPos();
        long chunkKey = ChunkPos.toLong(chunk.getPos().x, chunk.getPos().z);

        workerThread.submit(() -> {
            Set<BlockPos> foundBlocks = new HashSet<>();
            Map<Identifier, Integer> foundCounts = new HashMap<>();

            int worldHeight = Math.min(MINECRAFT.world.getHeight(), 256);
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < worldHeight; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockPos pos = chunkStart.add(x, y, z);
                        BlockState state = MINECRAFT.world.getBlockState(pos);
                        Block block = state.getBlock();

                        Identifier id = Registries.BLOCK.getId(block);
                        if (id != null && candidateBlockIds.contains(id)) {
                            foundBlocks.add(pos.toImmutable());
                            foundCounts.put(id, foundCounts.getOrDefault(id, 0) + 1);
                        }
                    }
                }
            }

            if (!foundBlocks.isEmpty()) {
                chunkBlocks.put(chunkKey, foundBlocks);

                if (notifier.get()) {
                    StringBuilder message = new StringBuilder("§cFound: ");
                    foundCounts.forEach((id, count) -> message.append(count).append("x ").append(id.getPath()).append(", "));
                    if (message.length() > 2) message.setLength(message.length() - 2);

                    MINECRAFT.execute(() -> CHAT_MANAGER.sendRaw(message.toString()));
                }
            } else {
                chunkBlocks.remove(chunkKey);
            }
        });

    }

    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MINECRAFT.player == null || MINECRAFT.world == null) return;

        MatrixStack matrices = event.getMatrixStack();
        BlockPos playerPos = MINECRAFT.player.getBlockPos();

        for (Set<BlockPos> blockSet : chunkBlocks.values()) {
            for (BlockPos pos : blockSet) {
                double distSq = playerPos.getSquaredDistance(pos);
                if (distSq > 168 * 168) continue;

                BlockState state = MINECRAFT.world.getBlockState(pos);
                Color blockColor = BlockUtil.getColorByBlockId(state);


                RenderUtil.drawBlockShape(
                        matrices,
                        MINECRAFT.world,
                        pos,
                        state,
                        new Color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 60),
                        blockColor,
                        lineWidth.get(),
                        filled.get()
                );
            }
        }
    }

    @Override
    public void onDisable() {
        chunkBlocks.clear();
        workerThread.shutdownNow();
    }

    @Override
    public void onEnable() {
        if (workerThread.isShutdown() || workerThread.isTerminated()) {
            CHAT_MANAGER.sendPersistent(SearchModule.class.getName(),"Restarting worker thread");
            workerThread = Executors.newSingleThreadExecutor();
        }
    }
}
