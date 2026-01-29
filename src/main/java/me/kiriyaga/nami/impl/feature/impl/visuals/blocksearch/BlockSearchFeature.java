package me.kiriyaga.nami.impl.feature.impl.visuals.blocksearch;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.ChunkDataEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.WhitelistSetting;
import me.kiriyaga.nami.util.BlockUtils;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import static me.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class BlockSearchFeature extends Feature {

    private final WhitelistSetting blockList = addSetting(new WhitelistSetting("Whitelist", true, WhitelistSetting.Type.BLOCK));
    private final BoolSetting fill = addSetting(new BoolSetting("Fill", true));
    private final BoolSetting tracers = addSetting(new BoolSetting("Tracers", false));
    private final BoolSetting notAtSpawn = addSetting(new BoolSetting("NotAtSpawn", false));

    private Set<Identifier> candidateIds = new HashSet<>();
    private final BlockingQueue<ChunkSnapshot> snapshotQueue = new LinkedBlockingQueue<>();
    public static final ConcurrentMap<Long, Set<BlockPos>> chunkBlocks = new ConcurrentHashMap<>();

    private BlockSearch worker;

    public BlockSearchFeature() {
        super("BlockSearch", "Search for specified blocks.", FeatureCategory.of("Render"));

        blockList.setOnChanged(this::updateCandidateBlocks);
        notAtSpawn.setOnChanged(this::reloadChunks);
    }

    @Override
    public void onEnable() {
        updateCandidateBlocks();

        worker = new BlockSearch(snapshotQueue, chunkBlocks);
        worker.start();
        reloadChunks();
    }

    @Override
    public void onDisable() {
        if (worker != null) worker.stopWorker();
        snapshotQueue.clear();
        chunkBlocks.clear();
    }

    private void updateCandidateBlocks() {
        candidateIds = blockList.getWhitelist();
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onChunkLoad(ChunkDataEvent event) {
        if (MC.level == null || MC.player == null) return;

        if (notAtSpawn.get()) {
            BlockPos p = MC.player.blockPosition();
            if (Math.abs(p.getX()) + Math.abs(p.getZ()) < 12000) return;
        }

        snapshotQueue.offer(makeSnapshot(event.getChunk()));

        chunkBlocks.keySet().removeIf(chunkKey -> {
            int chunkX = ChunkPos.getX(chunkKey);
            int chunkZ = ChunkPos.getZ(chunkKey);
            return !MC.level.hasChunk(chunkX, chunkZ);
        });
    }

    private ChunkSnapshot makeSnapshot(LevelChunk chunk) {
        ChunkPos pos = chunk.getPos();
        Set<BlockPos> found = new HashSet<>();
        LevelChunkSection[] sections = chunk.getSections();
        for (int secIndex = 0; secIndex < sections.length; secIndex++) {
            LevelChunkSection section = sections[secIndex];
            if (section == null || section.hasOnlyAir()) continue;

            int baseY = chunk.getSectionYFromSectionIndex(secIndex) << 4;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {

                        var state = section.getBlockState(x, y, z);
                        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());

                        if (!candidateIds.contains(id)) continue;

                        found.add(new BlockPos(
                                pos.getMinBlockX() + x,
                                baseY + y,
                                pos.getMinBlockZ() + z
                        ));
                    }
                }
            }
        }
        return new ChunkSnapshot(pos, found);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRender(Render3DEvent event) {
        if (MC.level == null || MC.player == null) return;
        Camera camera = MC.gameRenderer.getMainCamera();

        Vec3 camPos = camera.position();
        Vec3 start = camPos.add(Vec3.directionFromRotation(camera.xRot(), camera.yRot()));


        for (Set<BlockPos> set : chunkBlocks.values()) {
            for (BlockPos pos : set) {
                var state = MC.level.getBlockState(pos);
                Color color = BlockUtils.getColorByBlockId(state);

                RenderUtil.drawBlockPosLines(MC.level, pos, state, color, fill.get(), true, 1.5f);

                if (tracers.get()) {
                    RenderUtil.drawLine(start, Vec3.atCenterOf(pos), color, 1.5f);
                }
            }
        }
    }

    private void reloadChunks() {
        if (MC.level == null || MC.player == null) return;

        snapshotQueue.clear();
        chunkBlocks.clear();
        int radius = MC.options.renderDistance().get();
        ChunkPos pc = new ChunkPos(MC.player.blockPosition());

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                snapshotQueue.offer(makeSnapshot(MC.level.getChunk(pc.x + dx, pc.z + dz)));
            }
        }
    }
}