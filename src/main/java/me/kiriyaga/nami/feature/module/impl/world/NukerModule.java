package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class NukerModule extends Module {

    public enum Mode {
        FLOOR,
        AROUND
    }

    public final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("mode", Mode.AROUND));
    public final IntSetting radius = addSetting(new IntSetting("radius", 3, 1, 6));

    private final Set<BlockPos> targetBlocks = new LinkedHashSet<>();

    public NukerModule() {
        super("nuker", "Breaks blocks around or below you.", ModuleCategory.of("world"));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreTickEvent(PreTickEvent ev) {
        if (MC.player == null || MC.world == null) return;

        BlockPos playerPos = MC.player.getBlockPos();
        int r = radius.get();

        Iterator<BlockPos> it = targetBlocks.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next();
            if (pos.isWithinDistance(MC.player.getPos(), r + 0.5)) {
            } else {
                BREAK_MANAGER.getRequestHandler().removeBlock(pos);
                it.remove();
            }
        }

        switch (mode.get()) {
            case FLOOR -> {
                BlockPos base = playerPos.down();
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos checkPos = base.add(x, 0, z);
                        checkAndAddBlock(checkPos);
                    }
                }
            }
            case AROUND -> {
                BlockPos base = new BlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());
                for (int x = -r; x <= r; x++) {
                    for (int y = 0; y <= r; y++) {
                        for (int z = -r; z <= r; z++) {
                            BlockPos checkPos = base.add(x, y, z);
                            if (checkPos.equals(playerPos)) continue;
                            checkAndAddBlock(checkPos);
                        }
                    }
                }
            }
        }
    }

    private void checkAndAddBlock(BlockPos pos) {
        BlockState state = MC.world.getBlockState(pos);
        if (!state.isAir() && state.getBlock() != Blocks.BEDROCK) {
            if (targetBlocks.add(pos)) {
                BREAK_MANAGER.getRequestHandler().addBlock(pos);
            }
        } else {
            if (targetBlocks.remove(pos)) {
                BREAK_MANAGER.getRequestHandler().removeBlock(pos);
            }
        }
    }
}