package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.feature.setting.impl.WhitelistSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.placeBlock;

@RegisterModule
public class ScaffoldModule extends Module {

    private final IntSetting delay = addSetting(new IntSetting("Delay", 0, 0, 5));
    private final IntSetting shiftTicks = addSetting(new IntSetting("ShiftTicks", 1, 1, 8));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    private final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));
    private final BoolSetting simulate = addSetting(new BoolSetting("Simulate", false));
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
        BlockPos[] corners = getPlacements();
        int blocksPlaced = 0;
        int slot = getSelectedSlot();
        if (slot == -1) return;

        for (BlockPos pos : corners) {
            BlockPos targetPos = pos.down();

            if (placeBlock(targetPos, slot, rotate.get(), strictDirection.get(), simulate.get(), swing.get()))
                blocksPlaced++;

            if (blocksPlaced >= shiftTicks.get()) break;
        }

        if (blocksPlaced > 0) cooldown = delay.get();
    }

    private int getSelectedSlot() {
        int selectedSlot = MC.player.getInventory().getSelectedSlot();

        if (!MC.player.getInventory().getStack(selectedSlot).isEmpty()) {
            Block block = Block.getBlockFromItem(MC.player.getInventory().getStack(selectedSlot).getItem());
            if (block != Blocks.AIR) {
                Identifier blockId = Registries.BLOCK.getId(block);
                if (!whitelist.get() || whitelist.isWhitelisted(blockId)) {
                    return selectedSlot;
                }
            }
        }

        for (int i = 0; i < 9; i++) {
            if (MC.player.getInventory().getStack(i).isEmpty()) continue;

            Block block = Block.getBlockFromItem(MC.player.getInventory().getStack(i).getItem());
            if (block == Blocks.AIR) continue;

            Identifier blockId = Registries.BLOCK.getId(block);
            if (whitelist.get() && !whitelist.isWhitelisted(blockId)) continue;

            return i;
        }

        return -1;
    }

    private BlockPos[] getPlacements() {
        double minX = MC.player.getBoundingBox().minX;
        double maxX = MC.player.getBoundingBox().maxX;
        double minZ = MC.player.getBoundingBox().minZ;
        double maxZ = MC.player.getBoundingBox().maxZ;
        int y = (int) Math.floor(MC.player.getY());

        BlockPos[] possiblePositions = new BlockPos[] {new BlockPos((int) Math.floor(minX), y, (int) Math.floor(minZ)), new BlockPos((int) Math.floor(minX), y, (int) Math.floor(maxZ)), new BlockPos((int) Math.floor(maxX), y, (int) Math.floor(minZ)), new BlockPos((int) Math.floor(maxX), y, (int) Math.floor(maxZ))};

        return Arrays.stream(possiblePositions)
                .filter(pos -> MC.world.getBlockState(pos.down()).isAir()) // yes i know its cringe sorry
                .toArray(BlockPos[]::new);
    }

}