package me.kiriyaga.nami.impl.feature.impl.combat;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.feature.impl.client.ColorFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import me.kiriyaga.nami.impl.setting.impl.EnumSetting;
import me.kiriyaga.nami.impl.setting.impl.IntSetting;
import me.kiriyaga.nami.util.InteractionUtils;
import me.kiriyaga.nami.util.entity.TargetUtils;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static me.kiriyaga.nami.Nami.*;

@RegisterFeature
public class AutoWebFeature extends Feature {

    public enum PlaceMode { LEGS, HEAD, BOTH }
    public enum Item { COBWEB, SCAFFOLD }

    public final DoubleSetting range = addSetting(new DoubleSetting("Range", 3.00, 1.0, 6.0));
    private final EnumSetting<PlaceMode> placeMode = addSetting(new EnumSetting<>("PlaceMode", PlaceMode.LEGS));
    private final EnumSetting<Item> item = addSetting(new EnumSetting<>("Item", Item.COBWEB));
    private final BoolSetting selfToggle = addSetting(new BoolSetting("SelfToggle", true));
    private final IntSetting delay = addSetting(new IntSetting("Delay", 1, 0, 5));
    private final IntSetting shiftTicks = addSetting(new IntSetting("ShiftTicks", 1, 1, 8));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    private final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));
    private final BoolSetting multiTask = addSetting(new BoolSetting("MultiTask", false));
    private final BoolSetting simulate = addSetting(new BoolSetting("Simulate", false));
    private final BoolSetting render = addSetting(new BoolSetting("Render", false));

    private int cooldown = 0;
    private BlockPos renderPos = null;

    public AutoWebFeature() {
        super("AutoWeb", "Automatically places webs around target.", FeatureCategory.of("Combat"));
    }

    @SubscribeEvent
    public void onPreTickEvent(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        Entity target = TargetUtils.getTarget();
        if (target == null) {
            renderPos = null;
            return;
        }

        int slot = findSlot();
        if (slot == -1) {
            renderPos = null;
            return;
        }

        List<BlockPos> positions = getPositions(target);
        int placed = 0;

        for (BlockPos pos : positions) {
            if (MC.level.getBlockState(pos).isAir()) {
                renderPos = pos;
                InteractionUtils.placeBlock(pos, slot,range.get(), rotate.get(), strictDirection.get(), simulate.get(), swing.get(), this.name, multiTask.get());
                placed++;
                if (placed >= shiftTicks.get()) break;
            }
        }

        if (placed > 0) {
            cooldown = delay.get();
        } else if (selfToggle.get()) {
            toggle();
        }
    }

    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MC.player == null || MC.level == null || renderPos == null || !render.get()) return;

        ColorFeature colorFeature = FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class);
        Color color = colorFeature.getStyledGlobalColor();

        AABB box = new AABB(renderPos);

        RenderUtil.drawBoxLines(box, color, true, true, 1.5f);
    }

    private int findSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);

            if (item.get() == Item.COBWEB)
                if (!stack.isEmpty() && stack.getItem() == Blocks.COBWEB.asItem()) {
                return i;
            }

            if (item.get() == Item.SCAFFOLD)
                if (!stack.isEmpty() && stack.getItem() == Blocks.SCAFFOLDING.asItem()) {
                    return i;
                }
        }
        return -1;
    }

    private List<BlockPos> getPositions(Entity target) {
        double minX = target.getBoundingBox().minX;
        double maxX = target.getBoundingBox().maxX;
        double minZ = target.getBoundingBox().minZ;
        double maxZ = target.getBoundingBox().maxZ;

        int yLegs = (int) Math.floor(target.getY());
        int yHead = (int) Math.floor(target.getY() + 1);

        List<BlockPos> positions = new ArrayList<>();

        if (placeMode.get() == PlaceMode.LEGS || placeMode.get() == PlaceMode.BOTH) {
            positions.add(new BlockPos((int) Math.floor(minX), yLegs, (int) Math.floor(minZ)));
            positions.add(new BlockPos((int) Math.floor(minX), yLegs, (int) Math.floor(maxZ)));
            positions.add(new BlockPos((int) Math.floor(maxX), yLegs, (int) Math.floor(minZ)));
            positions.add(new BlockPos((int) Math.floor(maxX), yLegs, (int) Math.floor(maxZ)));
        }

        if (placeMode.get() == PlaceMode.HEAD || placeMode.get() == PlaceMode.BOTH) {
            positions.add(new BlockPos((int) Math.floor(minX), yHead, (int) Math.floor(minZ)));
            positions.add(new BlockPos((int) Math.floor(minX), yHead, (int) Math.floor(maxZ)));
            positions.add(new BlockPos((int) Math.floor(maxX), yHead, (int) Math.floor(minZ)));
            positions.add(new BlockPos((int) Math.floor(maxX), yHead, (int) Math.floor(maxZ)));
        }

        return positions;
    }
}
