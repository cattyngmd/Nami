package me.kiriyaga.essentials.feature.module.impl.render;

import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.ChunkDataEvent;
import me.kiriyaga.essentials.event.impl.Render3DEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.Chunk;

import java.awt.*;
        import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.kiriyaga.essentials.Essentials.*;

public class SearchModule extends Module {

    private final Set<BlockPos> trackedBlocks = new HashSet<>();

    public final BoolSetting filled = addSetting(new BoolSetting("Filled", false));
    public final DoubleSetting lineWidth = addSetting(new DoubleSetting("Line Width", 1.5, 0.5, 2.5));

    public SearchModule() {
        super("Search", "Searchsssss", Category.RENDER);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent event) {
        if (MINECRAFT.world == null) return;

        Chunk chunk = event.getChunk();
        BlockPos chunkStart = chunk.getPos().getStartPos();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < MINECRAFT.world.getHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = chunkStart.add(x, y, z);
                    BlockState state = MINECRAFT.world.getBlockState(pos);

                    // todo block list and command
                }
            }
        }
    }

    // todo: block update event

    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MINECRAFT.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        
        for (BlockPos pos : trackedBlocks) {
            double distanceSq = MINECRAFT.player.getBlockPos().getSquaredDistance(pos);
            if (distanceSq > 100 * 100) continue; // Ограничим рендер дальностью (100 блоков)

            Box box = new Box(pos);
            if (filled.get()) {
                RenderUtil.drawBoxFilled(matrices, box, Color.CYAN);
            } else {
                RenderUtil.drawBox(matrices, box, Color.CYAN, lineWidth.get());
            }
        }
    }
    // todo: block update event
}
