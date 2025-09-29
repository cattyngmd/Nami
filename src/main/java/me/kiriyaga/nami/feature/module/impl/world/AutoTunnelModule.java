package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class AutoTunnelModule extends Module {

    public enum TunnelMode {
        P1x1,
        P1x2,
        P1x3,
        P3x3
    }

    // TODO: corners

    public final EnumSetting<TunnelMode> mode = addSetting(new EnumSetting<>("Mode", TunnelMode.P1x2));

    private final Set<BlockPos> targets = new HashSet<>();

    public AutoTunnelModule() {
        super("AutoTunnel", "Automatically tunnels blocks in front of you.", ModuleCategory.of("World"));
    }

    @Override
    public void onDisable() {
        targets.forEach(pos -> BREAK_MANAGER.getRequestHandler().removeBlock(pos));
        targets.clear();
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPreTickEvent(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        BlockPos playerPos = MC.player.getBlockPos();
        Set<BlockPos> validTargets = new HashSet<>();

        for (int d = 1; d <= 1; d++) {
            BlockPos forward = playerPos.offset(MC.player.getHorizontalFacing(), d);

            switch (mode.get()) {
                case P1x1 -> addBlockIfBreakable(validTargets, forward);
                case P1x2 -> {
                    addBlockIfBreakable(validTargets, forward);
                    addBlockIfBreakable(validTargets, forward.up());
                }
                case P1x3 -> {
                    addBlockIfBreakable(validTargets, forward);
                    addBlockIfBreakable(validTargets, forward.up());
                    addBlockIfBreakable(validTargets, forward.up(2));
                }
                case P3x3 -> {
                    for (int x = -1; x <= 1; x++) {
                        for (int y = 0; y <= 2; y++) {
                            for (int z = -1; z <= 1; z++) {
                                BlockPos checkPos = forward.add(x, y, z);
                                addBlockIfBreakable(validTargets, checkPos);
                            }
                        }
                    }
                }
            }
        }

        targets.removeIf(pos -> {
            if (!validTargets.contains(pos)) {
                BREAK_MANAGER.getRequestHandler().removeBlock(pos);
                return true;
            }
            return false;
        });

        for (BlockPos pos : validTargets) {
            if (!targets.contains(pos)) {
                BREAK_MANAGER.getRequestHandler().addBlock(pos);
                targets.add(pos);
            }
        }
    }

    private void addBlockIfBreakable(Set<BlockPos> set, BlockPos pos) {
        BlockState state = MC.world.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.BEDROCK) return;

        set.add(pos);
    }
}
