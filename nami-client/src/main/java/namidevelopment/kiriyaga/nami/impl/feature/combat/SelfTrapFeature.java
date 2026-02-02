package namidevelopment.kiriyaga.nami.impl.feature.combat;

import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.event.impl.Render3DEvent;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.feature.client.ColorFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;
import namidevelopment.kiriyaga.nami.util.InteractionUtils;
import namidevelopment.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.nami.util.BlockUtils.getSurround;
import static namidevelopment.kiriyaga.nami.util.InteractionUtils.isPlaceable;
import static namidevelopment.kiriyaga.nami.util.InteractionUtils.isReplaceable;

@RegisterFeature
public class SelfTrapFeature extends Feature {

    public final DoubleSetting range = addSetting(new DoubleSetting("Range", 3.00, 1.0, 6.0));
    public final BoolSetting face = addSetting(new BoolSetting("Face", true));
    public final IntSetting delay = addSetting(new IntSetting("Delay", 0, 0, 5));
    public final IntSetting shiftTicks = addSetting(new IntSetting("ShiftTicks", 1, 1, 8));
    public final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    public final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));
    public final BoolSetting swapBack = addSetting(new BoolSetting("SwapBack", true));
    public final BoolSetting multiTask = addSetting(new BoolSetting("MultiTask", false));
    public final BoolSetting simulate = addSetting(new BoolSetting("Simulate", false));
    public final BoolSetting extension = addSetting(new BoolSetting("Extension", false));
    public final BoolSetting swing = addSetting(new BoolSetting("Swing", false));
    public final BoolSetting render = addSetting(new BoolSetting("Render", true));
    public final BoolSetting jumpDisable = addSetting(new BoolSetting("JumpDisable", false));
    public final BoolSetting selfToggle = addSetting(new BoolSetting("SelfToggle", false));

    private int cooldown = 0;
    private List<BlockPos> surroundPositions = new ArrayList<>();

    public SelfTrapFeature() {
        super("SelfTrap", "Traps you to prevent damage.", FeatureCategory.of("Combat"), "selftrap");
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

        if (jumpDisable.get() && !MC.player.onGround()) {
            this.toggle();
            return;
        }

        this.addDisplayInfo(surroundPositions.size()+"");
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        int blocksPlaced = 0;
        surroundPositions = getSurround(MC.player, face.get() && !MC.player.isVisuallyCrawling() ? 1 : 0, extension.get());

        if (surroundPositions.isEmpty() && selfToggle.get()) {
            this.toggle();
            return;
        }

        for (BlockPos pos : surroundPositions) {
            if (MC.level.getBlockState(pos).canBeReplaced()) {
                BlockPos foundation = pos.below();
                if (MC.level.getBlockState(foundation).canBeReplaced()) {
                    if ( InteractionUtils.placeBlock(foundation, getSlot(),swapBack.get(), range.get(), rotate.get(), strictDirection.get(), simulate.get(), swing.get(), this.name, multiTask.get())) {
                        blocksPlaced++;
                        if (blocksPlaced >= shiftTicks.get()) break;
                    }
                }

                if (InteractionUtils.placeBlock(pos, getSlot(), swapBack.get(), range.get(), rotate.get(), strictDirection.get(), simulate.get(), swing.get(), this.name, multiTask.get())) {
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

    private Item getSlot() {
        if (MC.player == null) return null;

        if (MC.player.getOffhandItem().getItem() instanceof BlockItem b){
            if (b.getBlock().getExplosionResistance() >= 600.00f)
                return MC.player.getOffhandItem().getItem();
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