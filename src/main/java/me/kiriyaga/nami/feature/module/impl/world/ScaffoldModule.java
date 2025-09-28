package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.feature.setting.impl.WhitelistSetting;
import me.kiriyaga.nami.util.InteractionUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.placeBlock;

@RegisterModule
public class ScaffoldModule extends Module {

    private final IntSetting delay = addSetting(new IntSetting("Delay", 0, 0, 5));
    private final IntSetting blocksPerTick = addSetting(new IntSetting("ShiftTicks", 1, 1, 8));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    private final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", false));
    public final WhitelistSetting whitelist = addSetting(new WhitelistSetting("WhiteList", false, WhitelistSetting.Type.BLOCK));

    private int cooldown = 0;

    public ScaffoldModule() {
        super("Scaffold", "Automatically scaffolds using specified blocks.", ModuleCategory.of("World"));
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

        BlockPos feetPos = MC.player.getBlockPos().down();

        int blocksPlaced = 0;
        int slot = getBlockSlot();
        if (slot == -1) return;

        BlockPos targetPos = feetPos;
        BlockState targetState = MC.world.getBlockState(targetPos);

        if (!targetState.isAir()) return;

        if (placeBlock(targetPos, slot, rotate.get(), strictDirection.get(), swing.get())) {
            blocksPlaced++;
        }

        if (blocksPlaced > 0) cooldown = delay.get();
    }

    private int getBlockSlot() {
        boolean useWhitelist = whitelist.get();

        for (int i = 0; i < 9; i++) {
            if (MC.player.getInventory().getStack(i).isEmpty()) continue;
            Block block = Block.getBlockFromItem(MC.player.getInventory().getStack(i).getItem());
            if (block == Blocks.AIR) continue;
            Identifier blockId = Registries.BLOCK.getId(block);
            if (useWhitelist && !whitelist.isWhitelisted(blockId)) continue;

            return i;
        }

        return -1;
    }
}
