package namidevelopment.kiriyaga.nami.impl.feature.combat.component;

import namidevelopment.kiriyaga.api.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.event.impl.Render3DEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.api.model.setting.DoubleSetting;
import namidevelopment.kiriyaga.api.model.setting.IntSetting;
import namidevelopment.kiriyaga.api.util.InteractionUtils;
import namidevelopment.kiriyaga.api.util.render.RenderUtil;
import namidevelopment.kiriyaga.nami.impl.feature.client.ColorFeature;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static namidevelopment.kiriyaga.api.NamiApi.FEATURE_SERVICE;
import static namidevelopment.kiriyaga.api.NamiApi.MC;

public class TrapComponent {

    public final DoubleSetting range;
    public final IntSetting delay;
    public final IntSetting shiftTicks;
    public final BoolSetting rotate;
    public final BoolSetting strictDirection;
    public final BoolSetting swapBack;
    public final BoolSetting multiTask;
    public final BoolSetting simulate;
    public final BoolSetting swing;
    public final BoolSetting render;

    private int cooldown = 0;
    private final List<BlockPos> targetPositions = new ArrayList<>();

    public TrapComponent(Feature feature) {
        range = feature.addSetting(new DoubleSetting("Range", 4.50, 1.0, 6.0));
        delay = feature.addSetting(new IntSetting("Delay", 0, 0, 5));
        shiftTicks = feature.addSetting(new IntSetting("ShiftTicks", 1, 1, 8));
        rotate = feature.addSetting(new BoolSetting("Rotate", true));
        strictDirection = feature.addSetting(new BoolSetting("StrictDirection", true));
        swapBack = feature.addSetting(new BoolSetting("SwapBack", true));
        multiTask = feature.addSetting(new BoolSetting("MultiTask", false));
        simulate = feature.addSetting(new BoolSetting("Simulate", false));
        swing = feature.addSetting(new BoolSetting("Swing", true));
        render = feature.addSetting(new BoolSetting("Render", true));
    }

    public void onDisable() {
        cooldown = 0;
        targetPositions.clear();
    }

    public List<BlockPos> getTargetPositions() {
        return targetPositions;
    }

    public void onTick(PreTickEvent event, Feature owner, List<BlockPos> newTargets) {
        if (MC.player == null || MC.level == null) return;

        targetPositions.clear();
        if (newTargets != null) targetPositions.addAll(newTargets);

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        int blocksPlaced = 0;

        for (BlockPos pos : targetPositions) {
            if (!MC.level.getBlockState(pos).canBeReplaced()) continue;

            BlockPos foundation = pos.below();
            if (MC.level.getBlockState(foundation).canBeReplaced()) {
                if (InteractionUtils.placeBlock(foundation, getSlot(), swapBack.get(), range.get(), rotate.get(), strictDirection.get(), simulate.get(), swing.get(), owner.getName(), multiTask.get())) {
                    blocksPlaced++;
                    if (blocksPlaced >= shiftTicks.get()) break;
                }
            }

            if (InteractionUtils.placeBlock(pos, getSlot(), swapBack.get(), range.get(), rotate.get(), strictDirection.get(), simulate.get(), swing.get(), owner.getName(), multiTask.get())) {
                blocksPlaced++;
                if (blocksPlaced >= shiftTicks.get()) break;
            }
        }

        if (blocksPlaced > 0) {
            cooldown = delay.get();
        }
    }

    public void onRender(Render3DEvent event) {
        if (MC.player == null || MC.level == null) return;
        if (!render.get()) return;
        if (targetPositions.isEmpty()) return;

        ColorFeature colorFeature = FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class);
        Color color = colorFeature.getStyledGlobalColor();

        for (BlockPos pos : targetPositions) {
            AABB box = new AABB(pos);
            RenderUtil.drawBoxLines(box, color, true, true, 1.5f);
        }
    }

    private Item getSlot() {
        if (MC.player == null) return null;

        if (MC.player.getOffhandItem().getItem() instanceof BlockItem b) {
            if (b.getBlock().getExplosionResistance() >= 600.0f)
                return MC.player.getOffhandItem().getItem();
        }

        if (MC.player.getMainHandItem().getItem() instanceof BlockItem b) {
            if (b.getBlock().getExplosionResistance() >= 600.0f)
                return MC.player.getMainHandItem().getItem();
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (item instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block.getExplosionResistance() >= 600.0f) {
                    return item;
                }
            }
        }

        return null;
    }
}
