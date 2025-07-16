package me.kiriyaga.nami.feature.module.impl.render;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.ChunkDataEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import me.kiriyaga.nami.setting.impl.WhitelistSetting;
import me.kiriyaga.nami.util.BlockUtil;
import me.kiriyaga.nami.util.render.RenderUtil;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.MC;

public class SearchModule extends Module {

    private final BoolSetting lazyLoadEnabled = addSetting(new BoolSetting("lazy load", true));
    private final IntSetting chunksPerTick = addSetting(new IntSetting("count", 1, 1, 5));
    private final IntSetting cooldownTicks = addSetting(new IntSetting("delay", 2, 0, 20));
    private final WhitelistSetting whitelist = addSetting(new WhitelistSetting("whitelist", false, this.name));
    private final BoolSetting storages = addSetting(new BoolSetting("storages", true));
    private final BoolSetting nonVanilla = addSetting(new BoolSetting("non-vanilla", false));
    private final BoolSetting notifier = addSetting(new BoolSetting("notifier", false));
    private final BoolSetting notAtSpawn = addSetting(new BoolSetting("not at spawn", false));
    private final DoubleSetting lineWidth = addSetting(new DoubleSetting("line width", 1.5, 0.5, 3));
    private final BoolSetting filled = addSetting(new BoolSetting("filled", true));

    private final ConcurrentMap<Long, Set<BlockPos>> chunkBlocks = new ConcurrentHashMap<>();
    private final Queue<Chunk> pendingChunks = new LinkedList<>();

    private Set<Identifier> candidateBlockIds = new HashSet<>();
    private final Queue<String> pendingMessages = new LinkedList<>();

    private int tickCounter = 0;

    public SearchModule() {
        super("search", "Search certain blocks on loaded chunks.", Category.visuals, "srcj", "blockesp", "serch", "ыуфкср");
        whitelist.setOnChanged(this::reloadChunksAroundPlayer);
        storages.setOnChanged(this::reloadChunksAroundPlayer);
        nonVanilla.setOnChanged(this::reloadChunksAroundPlayer);
        notAtSpawn.setOnChanged(this::reloadChunksAroundPlayer);
        notifier.setOnChanged(this::reloadChunksAroundPlayer); // i just want it dont blame me
    }

    @Override
    public void onEnable() {
        reloadChunksAroundPlayer();
    }

    private void updateCandidateBlocks() {
        Set<Block> blocks = new HashSet<>();
        if (storages.get()) blocks.addAll(BlockUtil.getStorage());
        if (nonVanilla.get()) blocks.addAll(BlockUtil.getNonVanillaGeneratedBlocks());

        candidateBlockIds = blocks.stream()
                .map(Registries.BLOCK::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        candidateBlockIds.addAll(whitelist.getWhitelist());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onChunkLoad(ChunkDataEvent event) {
        if (MC.world == null || MC.player == null) return;

        if (notAtSpawn.get()) {
            BlockPos pos = MC.player.getBlockPos();
            if (Math.abs(pos.getX()) + Math.abs(pos.getZ()) < 5000) return;
        }

        updateCandidateBlocks();

        synchronized (pendingChunks) {
            pendingChunks.offer(event.getChunk());
        }
    }

    @SubscribeEvent
    public void onPosttick(PostTickEvent event) {
        if (MC.world == null || MC.player == null) return;

        if (!lazyLoadEnabled.get()) {
            Chunk chunk;
            synchronized (pendingChunks) {
                while ((chunk = pendingChunks.poll()) != null) {
                    processChunk(chunk);
                }
            }
            return;
        }

        tickCounter++;
        if (tickCounter < cooldownTicks.get()) return;
        tickCounter = 0;

        int count = 0;
        while (count < chunksPerTick.get()) {
            Chunk chunk;
            synchronized (pendingChunks) {
                chunk = pendingChunks.poll();
            }
            if (chunk == null) break;

            processChunk(chunk);
            count++;
        }
    }

    private void processChunk(Chunk chunk) {
        BlockPos chunkStart = chunk.getPos().getStartPos();
        long chunkKey = ChunkPos.toLong(chunk.getPos().x, chunk.getPos().z);

        Set<BlockPos> foundBlocks = new HashSet<>();
        Map<Identifier, Integer> foundCounts = new HashMap<>();

        int minY = -64;
        int maxY = 320;

        for (int x = 0; x < 16; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = new BlockPos(chunkStart.getX() + x, y, chunkStart.getZ() + z);
                    BlockState state = MC.world.getBlockState(pos);
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

                synchronized (pendingMessages) {
                    pendingMessages.offer(message.toString());
                }
            }
        } else {
            chunkBlocks.remove(chunkKey);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRender(Render3DEvent event) {
        if (MC.player == null || MC.world == null) return;

        synchronized (pendingMessages) {
            while (!pendingMessages.isEmpty()) {
                CHAT_MANAGER.sendRaw(pendingMessages.poll());
            }
        }

        MatrixStack matrices = event.getMatrices();
        BlockPos playerPos = MC.player.getBlockPos();

        for (Set<BlockPos> blockSet : chunkBlocks.values()) {
            for (BlockPos pos : blockSet) {
                if (playerPos.getSquaredDistance(pos) > 300 * 300) continue;

                BlockState state = MC.world.getBlockState(pos);
                Color color = BlockUtil.getColorByBlockId(state);

                RenderUtil.drawBlockShape(
                        matrices,
                        MC.world,
                        pos,
                        state,
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 60),
                        color,
                        lineWidth.get(),
                        filled.get()
                );
            }
        }
    }

    private void reloadChunksAroundPlayer() {
        if (MC.world == null || MC.player == null) return;

        synchronized (pendingChunks) {
            pendingChunks.clear();

            int radius = MC.options.getViewDistance().getValue();
            BlockPos playerPos = MC.player.getBlockPos();
            ChunkPos playerChunk = new ChunkPos(playerPos);

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Chunk chunk = MC.world.getChunk(playerChunk.x + dx, playerChunk.z + dz);
                    pendingChunks.offer(chunk);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        chunkBlocks.clear();
        synchronized (pendingChunks) {
            pendingChunks.clear();
        }
    }
}