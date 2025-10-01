package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.InteractionUtils;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class FeetTrapModule extends Module {

    public final DoubleSetting range = addSetting(new DoubleSetting("Range", 3.00, 1.0, 6.0));
    private final IntSetting delay = addSetting(new IntSetting("Delay", 0, 0, 5));
    private final IntSetting shiftTicks = addSetting(new IntSetting("ShiftTicks", 1, 1, 8));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    private final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));
    private final BoolSetting simulate = addSetting(new BoolSetting("Simulate", false));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", false));
    private final BoolSetting render = addSetting(new BoolSetting("Render", true));
    private final BoolSetting jumpDisable = addSetting(new BoolSetting("JumpDisable", false));

    private int cooldown = 0;

    private List<BlockPos> surroundPositions = new ArrayList<>();

    public FeetTrapModule() {
        super("FeetTrap", "Places blocks around your feet.", ModuleCategory.of("Combat"), "feettrap");
    }

    @Override
    public void onDisable() {
        cooldown = 0;
        surroundPositions.clear();
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (jumpDisable.get() && !MC.player.isOnGround()) {
            this.toggle();
            return;
        }

        this.setDisplayInfo(surroundPositions.size()+"");
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (MODULE_MANAGER.getStorage().getByClass(SelfTrapModule.class).isEnabled()) {
            surroundPositions.clear();
            return;
        }

        int blocksPlaced = 0;

        surroundPositions = getSurround(MC.player);

        for (BlockPos pos : surroundPositions) {
            if (MC.world.getBlockState(pos).isReplaceable()) {
                BlockPos foundation = pos.down();
                if (MC.world.getBlockState(foundation).isReplaceable()) {
                    int slot = getSlot();
                    if (slot != -1 && InteractionUtils.placeBlock(foundation, slot, range.get(), rotate.get(), strictDirection.get(), simulate.get(), swing.get(), this.name)) {
                        blocksPlaced++;
                        if (blocksPlaced >= shiftTicks.get()) break;
                    }
                }

                int slotTop = getSlot();
                if (slotTop != -1 && InteractionUtils.placeBlock(pos, slotTop, range.get(), rotate.get(), strictDirection.get(), simulate.get(), swing.get(), this.name)) {
                    blocksPlaced++;
                    if (blocksPlaced >= shiftTicks.get()) break;
                }
            }
        }

        if (blocksPlaced > 0) {
            cooldown = delay.get();
        }
    }

    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MC.player == null || MC.world == null || surroundPositions.isEmpty() || !render.get()) return;

        MatrixStack matrices = event.getMatrices();

        ColorModule colorModule = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
        Color color = colorModule.getStyledGlobalColor();
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 60);

        for (BlockPos pos : surroundPositions) {
            Box box = new Box(pos);
            RenderUtil.drawBox(matrices, box, fillColor, color, 1.5f, true, true);
        }
    }

    private List<BlockPos> getSurround(PlayerEntity player) {
        Set<BlockPos> positions = new HashSet<>();

        Box bb = player.getBoundingBox();
        int yLegs = (int) Math.floor(player.getY());
        List<BlockPos> inside = new ArrayList<>();
        for (int x = (int) Math.floor(bb.minX); x < Math.ceil(bb.maxX); x++) {
            for (int z = (int) Math.floor(bb.minZ); z < Math.ceil(bb.maxZ); z++) {
                inside.add(new BlockPos(x, yLegs, z));
            }
        }

        for (BlockPos base : inside)
            addSurroundForBase(base, positions);

        expand(positions, player);

        List<BlockPos> result = new ArrayList<>();
        for (BlockPos pos : positions)
            if (!hasEntity(pos))
                result.add(pos);

        return result;
    }

    private void addSurroundForBase(BlockPos base, Set<BlockPos> positions) {
        BlockPos below = base.down();
        addIfValid(below, positions);

        BlockPos north = base.north();
        BlockPos south = base.south();
        BlockPos east  = base.east();
        BlockPos west  = base.west();

        addIfValid(north, positions);
        addIfValid(south, positions);
        addIfValid(east, positions);
        addIfValid(west, positions);
    }

    private void addIfValid(BlockPos pos, Set<BlockPos> positions) {
        if (isReplaceable(pos)) {
            positions.add(pos);
        }
    }


    private void expand(Set<BlockPos> positions, PlayerEntity player) {
        Set<BlockPos> extra = new HashSet<>();

        for (BlockPos pos : positions) {
            Box blockBox = new Box(pos);
            for (Entity entity : MC.world.getEntities()) {
                if (entity.squaredDistanceTo(player) > 10) continue;
                if (entity instanceof EndCrystalEntity) continue;
                if (entity instanceof ItemEntity) continue;

                if (entity.getBoundingBox().intersects(blockBox)) {
                    int entY = (int) Math.floor(entity.getY());
                    Box entBox = entity.getBoundingBox();
                    for (int x = (int) Math.floor(entBox.minX); x < Math.ceil(entBox.maxX); x++) {
                        for (int z = (int) Math.floor(entBox.minZ); z < Math.ceil(entBox.maxZ); z++) {
                            BlockPos entBase = new BlockPos(x, entY, z);
                            addSurroundForBase(entBase, extra);
                        }
                    }
                }
            }
        }

        positions.addAll(extra);
    }

    private boolean hasEntity(BlockPos pos) {
        Box blockBox = new Box(pos);
        for (Entity entity : MC.world.getEntities()) {
            if (entity.squaredDistanceTo(MC.player) > 10) continue;
            if (entity instanceof EndCrystalEntity) continue;
            if (entity instanceof ItemEntity) continue;

            if (entity.getBoundingBox().intersects(blockBox)) {
                return true;
            }
        }
        return false;
    }

    private boolean isReplaceable(BlockPos pos) {
        return MC.world.getBlockState(pos).isReplaceable();
    }

    private int getSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block.getBlastResistance() >= 600.0f)
                    return i;
            }
        }
        return -1;
    }
}
