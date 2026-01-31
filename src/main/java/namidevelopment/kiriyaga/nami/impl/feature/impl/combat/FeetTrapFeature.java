package namidevelopment.kiriyaga.nami.impl.feature.impl.combat;

import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.event.impl.Render3DEvent;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.feature.impl.client.ColorFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;
import namidevelopment.kiriyaga.nami.util.InteractionUtils;
import namidevelopment.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.world.level.block.Block;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.nami.util.InteractionUtils.isPlaceable;
import static namidevelopment.kiriyaga.nami.util.InteractionUtils.isReplaceable;

@RegisterFeature
public class FeetTrapFeature extends Feature {

    public final DoubleSetting range = addSetting(new DoubleSetting("Range", 3.00, 1.0, 6.0));
    public final IntSetting delay = addSetting(new IntSetting("Delay", 0, 0, 5));
    public final IntSetting shiftTicks = addSetting(new IntSetting("ShiftTicks", 1, 1, 8));
    public final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    public final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));
    public final BoolSetting multiTask = addSetting(new BoolSetting("MultiTask", false));
    public final BoolSetting simulate = addSetting(new BoolSetting("Simulate", false));
    public final BoolSetting swing = addSetting(new BoolSetting("Swing", false));
    public final BoolSetting extension = addSetting(new BoolSetting("Extension", false));
    public final BoolSetting render = addSetting(new BoolSetting("Render", true));
    public final BoolSetting jumpDisable = addSetting(new BoolSetting("JumpDisable", false));

    private int cooldown = 0;

    private List<BlockPos> surroundPositions = new ArrayList<>();

    public FeetTrapFeature() {
        super("FeetTrap", "Places blocks around your feet.", FeatureCategory.of("Combat"), "feettrap");
    }

    @Override
    public void onDisable() {
        cooldown = 0;
        surroundPositions.clear();
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;
        this.clearDisplayInfo();

        if (FEATURE_SERVICE.getStorage().getByClass(SelfTrapFeature.class).isEnabled())
            return;

        if (jumpDisable.get() && !MC.player.onGround()) {
            this.toggle();
            return;
        }

        this.addDisplayInfo(surroundPositions.size()+"");
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (FEATURE_SERVICE.getStorage().getByClass(SelfTrapFeature.class).isEnabled()) {
            surroundPositions.clear();
            return;
        }

        int blocksPlaced = 0;

        surroundPositions = getSurround(MC.player);

        for (BlockPos pos : surroundPositions) {
            if (MC.level.getBlockState(pos).canBeReplaced()) {
                BlockPos foundation = pos.below();
                if (MC.level.getBlockState(foundation).canBeReplaced()) {
                    int slot = getSlot();
                    if (slot != -1 && InteractionUtils.placeBlock(foundation, slot, range.get(), rotate.get(), strictDirection.get(), simulate.get(), swing.get(), this.name, multiTask.get())) {
                        blocksPlaced++;
                        if (blocksPlaced >= shiftTicks.get()) break;
                    }
                }

                int slotTop = getSlot();
                if (slotTop != -1 && InteractionUtils.placeBlock(pos, slotTop, range.get(), rotate.get(), strictDirection.get(), simulate.get(), swing.get(), this.name, multiTask.get())) {
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
        if (MC.player == null || MC.level == null || surroundPositions.isEmpty() || !render.get()) return;

        PoseStack matrices = event.getMatrices();

        ColorFeature colorFeature = FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class);
        Color color = colorFeature.getStyledGlobalColor();

        for (BlockPos pos : surroundPositions) {
            AABB box = new AABB(pos);
            RenderUtil.drawBoxLines(box, color, true, true, 1.5f);
        }
    }

    private List<BlockPos> getSurround(Player player) {
        Set<BlockPos> positions = new HashSet<>();

        AABB bb = player.getBoundingBox();
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
            if (!isPlaceable(pos))
                result.add(pos);

        return result;
    }

    private void addSurroundForBase(BlockPos base, Set<BlockPos> positions) {
        BlockPos below = base.below();
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


    private void expand(Set<BlockPos> positions, Player player) {
        if (!extension.get())
            return;

        Set<BlockPos> extra = new HashSet<>();

        for (BlockPos pos : positions) {
            AABB blockBox = new AABB(pos);
            for (Entity entity : MC.level.entitiesForRendering()) {
                if (entity.distanceToSqr(player) > 10) continue;
                if (entity instanceof EndCrystal) continue;
                if (entity instanceof ItemEntity) continue;

                if (entity.getBoundingBox().intersects(blockBox)) {
                    int entY = (int) Math.floor(entity.getY());
                    AABB entBox = entity.getBoundingBox();
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

    private int getSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block.getExplosionResistance() >= 600.0f)
                    return i;
            }
        }
        return -1;
    }
}
