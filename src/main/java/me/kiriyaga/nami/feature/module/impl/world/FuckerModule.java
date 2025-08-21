package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class FuckerModule extends Module {

    public enum Mode {
        FARM,
        SUGAR_CANE
    }

    public final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("mode", Mode.FARM));
    public final IntSetting radius = addSetting(new IntSetting("radius", 3, 1, 6));

    private final Set<BlockPos> s = new HashSet<>();

    public FuckerModule() {
        super("fucker", "Automatically breaks selected type of blocks around you.", ModuleCategory.of("world"));
    }

    @Override
    public void onDisable() {
        s.forEach(pos -> BREAK_MANAGER.getRequestHandler().removeBlock(pos));
        s.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreTickEvent(PreTickEvent ev) {
        if (MC.player == null || MC.world == null) return;

        BlockPos playerPos = MC.player.getBlockPos();
        int r = radius.get();

        Set<BlockPos> validTargets = new HashSet<>();

        switch (mode.get()) {
//            case FLOOR -> {
//                BlockPos base = playerPos.down();
//                for (int x = -r; x <= r; x++) {
//                    for (int z = -r; z <= r; z++) {
//                        BlockPos checkPos = base.add(x, 0, z);
//                        addBlockToBreak(checkPos);
//                    }
//                }
//            }
//            case AROUND -> {
//                BlockPos base = playerPos;
//                for (int x = -r; x <= r; x++) {
//                    for (int y = -r; y <= r; y++) {
//                        for (int z = -r; z <= r; z++) {
//                            BlockPos checkPos = base.add(x, y, z);
//                            if (checkPos.getY() < playerPos.getY()) continue;
//                            if (checkPos.equals(playerPos)) continue;
//                            addBlockToBreak(checkPos);
//                        }
//                    }
//                }
//            }
            case FARM -> {
                for (int x = -r; x <= r; x++) {
                    for (int y = -r; y <= r; y++) {
                        for (int z = -r; z <= r; z++) {
                            BlockPos checkPos = playerPos.add(x, y, z);
                            if (checkPos.equals(playerPos)) continue;
                            if (isPlantBlock(checkPos)) {
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
                            BlockPos checkPos = playerPos.add(x, y, z);
                            if (checkPos.equals(playerPos)) continue;
                            if (isSugarCaneBlock(checkPos)) {
                                validTargets.add(checkPos);
                            }
                        }
                    }
                }
            }
        }

        s.removeIf(pos -> {
            if (!validTargets.contains(pos)) {
                BREAK_MANAGER.getRequestHandler().removeBlock(pos);
                return true;
            }
            return false;
        });

        for (BlockPos pos : validTargets) {
            if (!s.contains(pos)) {
                BREAK_MANAGER.getRequestHandler().addBlock(pos);
                s.add(pos);
            }
        }
    }

    private boolean isPlantBlock(BlockPos pos) {
        BlockState state = MC.world.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.BEDROCK || state.isAir()) return false;

        if (block instanceof CropBlock cropBlock) {
            return cropBlock.isMature(state);
        }

        if (block instanceof SweetBerryBushBlock) {
            Integer age = state.get(SweetBerryBushBlock.AGE);
            return age != null && age >= 3; // it should be 3 but im not sure TODO: check this
        }

        return false;
    }

    private boolean isSugarCaneBlock(BlockPos pos) {
        BlockState state = MC.world.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.BEDROCK || state.isAir()) return false;

        if (block instanceof SugarCaneBlock || block instanceof BambooBlock) {
            BlockPos belowPos = pos.down();
            Block belowBlock = MC.world.getBlockState(belowPos).getBlock();
            return belowBlock == block;
        }

        return false;
    }
}
