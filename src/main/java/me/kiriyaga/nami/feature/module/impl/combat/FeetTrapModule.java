package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

import me.kiriyaga.nami.util.InteractionUtils;
import net.minecraft.util.math.BlockPos;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.*;

@RegisterModule
public class FeetTrapModule extends Module {

    private final IntSetting delay = addSetting(new IntSetting("Delay", 0, 0, 5));
    private final IntSetting shiftTicks = addSetting(new IntSetting("ShiftTicks", 1, 1, 8));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    private final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", false));

    private int cooldown = 0;

    private static final BlockPos[] SURROUND = new BlockPos[] {
            new BlockPos(1, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(0, 0, -1)
    };

    public FeetTrapModule() {
        super("FeetTrap", "Places blocks around your feet to trap yourself.", ModuleCategory.of("World"), "feettrap");
    }

    @Override
    public void onDisable() {
        cooldown = 0;
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        BlockPos playerPos = MC.player.getBlockPos();

        int blocksPlaced = 0;

        for (BlockPos offset : SURROUND) {
            BlockPos targetPos = playerPos.add(offset.getX(), offset.getY(), offset.getZ());
            if (!MC.world.getBlockState(targetPos).isAir()) continue;

            int blockSlot = getBlockSlot();
            if (blockSlot == -1) continue;

            if (InteractionUtils.placeBlock(targetPos, blockSlot, rotate.get(), strictDirection.get(), swing.get())) {
                blocksPlaced++;
            }

            if (blocksPlaced >= shiftTicks.get()) break;
        }

        if (blocksPlaced > 0) {
            cooldown = delay.get();
        }
    }

    private int getBlockSlot() {
        for (int i = 0; i < 9; i++) {
            if (!MC.player.getInventory().getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
