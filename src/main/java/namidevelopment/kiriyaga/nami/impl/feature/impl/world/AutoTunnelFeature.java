package namidevelopment.kiriyaga.nami.impl.feature.impl.world;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.EnumSetting;
import namidevelopment.kiriyaga.nami.util.InteractionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterFeature
public class AutoTunnelFeature extends Feature {

    public enum TunnelMode {
        P1x1,
        P1x2,
        P1x3,
        P3x3
    }

    public final EnumSetting<TunnelMode> mode = addSetting(new EnumSetting<>("Mode", TunnelMode.P1x2));
    public final DoubleSetting distance = addSetting(new DoubleSetting("Range", 5.0, 1.0, 6.0));
    public final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    public final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    public final BoolSetting grim = addSetting(new BoolSetting("Grim", false));
    public final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));

    private final Set<BlockPos> cache = new HashSet<>();

    public AutoTunnelFeature() {
        super("AutoTunnel", "Automatically tunnels blocks in front of you.", FeatureCategory.of("World"));
    }

    @Override
    public void onDisable() {
        cache.clear();
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPreTickEvent(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;

        BlockPos playerPos = MC.player.blockPosition();
        Set<BlockPos> validTargets = new HashSet<>();

        BlockPos forward = playerPos.relative(MC.player.getDirection(), 1);

        switch (mode.get()) {
            case P1x1 -> addBlockIfBreakable(validTargets, forward);
            case P1x2 -> {
                addBlockIfBreakable(validTargets, forward);
                addBlockIfBreakable(validTargets, forward.above());
            }
            case P1x3 -> {
                addBlockIfBreakable(validTargets, forward);
                addBlockIfBreakable(validTargets, forward.above());
                addBlockIfBreakable(validTargets, forward.above(2));
            }
            case P3x3 -> {
                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y <= 2; y++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos checkPos = forward.offset(x, y, z);
                            addBlockIfBreakable(validTargets, checkPos);
                        }
                    }
                }
            }
        }

        BlockPos bestTarget = validTargets.stream()
                .min(Comparator.comparingDouble(a -> MC.player.distanceToSqr(
                        a.getX() + 0.5, a.getY() + 0.5, a.getZ() + 0.5)))
                .orElse(null);

        if (bestTarget != null) {
            InteractionUtils.breakBlock(
                    bestTarget,
                    distance.get(),
                    rotate.get(),
                    swing.get(),
                    grim.get(),
                    strictDirection.get(),
                    this.name
            );
        }
    }

    private void addBlockIfBreakable(Set<BlockPos> set, BlockPos pos) {
        BlockState state = MC.level.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.BEDROCK || state.isAir()) return;
        set.add(pos);
    }
}
