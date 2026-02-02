package namidevelopment.kiriyaga.nami.impl.feature.world;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.EnumSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;
import namidevelopment.kiriyaga.nami.util.InteractionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterFeature
public class FuckerFeature extends Feature {

    public enum Mode {
        FARM,
        SUGAR_CANE,
        GRASS
    }

    public final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("Mode", Mode.FARM));
    public final DoubleSetting distance = addSetting(new DoubleSetting("Range", 5.0, 1.0, 6.0));
    public final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    public final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    public final BoolSetting grim = addSetting(new BoolSetting("Grim", false));
    public final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));
    public final IntSetting radius = addSetting(new IntSetting("Radius", 3, 1, 6));

    private final Set<BlockPos> s = new HashSet<>();

    public FuckerFeature() {
        super("Fucker", "Automatically breaks selected type of blocks around you.", FeatureCategory.of("World"));
    }

    @Override
    public void onDisable() {
        s.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreTickEvent(PreTickEvent ev) {
        if (MC.player == null || MC.level == null) return;

        BlockPos playerPos = MC.player.blockPosition();
        int r = radius.get();

        Set<BlockPos> validTargets = new HashSet<>();

        switch (mode.get()) {
            case FARM -> {
                for (int x = -r; x <= r; x++) {
                    for (int y = -r; y <= r; y++) {
                        for (int z = -r; z <= r; z++) {
                            BlockPos checkPos = playerPos.offset(x, y, z);
                            if (checkPos.equals(playerPos)) continue;
                            if (isFarmPlant(checkPos)) {
                                validTargets.add(checkPos);
                            }
                        }
                    }
                }
            }
            case SUGAR_CANE -> {
                for (int x = -r; x <= r; x++) {
                    for (int y = -r; y <= r; y++) {
                        for (int z = -r; z <= r; z++) {
                            BlockPos checkPos = playerPos.offset(x, y, z);
                            if (checkPos.equals(playerPos)) continue;
                            if (isSugarCaneBlock(checkPos)) {
                                validTargets.add(checkPos);
                            }
                        }
                    }
                }
            }
            case GRASS -> {
                for (int x = -r; x <= r; x++) {
                    for (int y = -r; y <= r; y++) {
                        for (int z = -r; z <= r; z++) {
                            BlockPos checkPos = playerPos.offset(x, y, z);
                            if (checkPos.equals(playerPos)) continue;
                            if (isGrassLike(checkPos)) {
                                validTargets.add(checkPos);
                            }
                        }
                    }
                }
            }
        }

        BlockPos bestTarget = validTargets.stream()
                .min(Comparator.comparingDouble(a -> MC.player.distanceToSqr(a.getX() + 0.5, a.getY() + 0.5, a.getZ() + 0.5)))
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

    private boolean isFarmPlant(BlockPos pos) {
        BlockState state = MC.level.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.BEDROCK || state.isAir()) return false;

        if (block instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(state);
        }

        if (block instanceof SweetBerryBushBlock) {
            Integer age = state.getValue(SweetBerryBushBlock.AGE);
            return age != null && age >= 3;
        }

        if (block instanceof NetherWartBlock) {
            Integer age = state.getValue(NetherWartBlock.AGE);
            return age != null && age >= 3;  // it's 3 for a fully grown netherwart
        }

        return false;
    }

    private boolean isSugarCaneBlock(BlockPos pos) {
        BlockState state = MC.level.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.BEDROCK || state.isAir()) return false;

        if (block instanceof SugarCaneBlock || block instanceof BambooStalkBlock) {
            BlockPos belowPos = pos.below();
            Block belowBlock = MC.level.getBlockState(belowPos).getBlock();
            return belowBlock == block;
        }

        return false;
    }

    private boolean isGrassLike(BlockPos pos) {
        BlockState state = MC.level.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.BEDROCK || state.isAir()) return false;

        return block instanceof VegetationBlock;
    }
}