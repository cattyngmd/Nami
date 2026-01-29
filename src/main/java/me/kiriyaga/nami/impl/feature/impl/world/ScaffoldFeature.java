package me.kiriyaga.nami.impl.feature.impl.world;

import me.kiriyaga.nami.api.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.feature.impl.client.ColorFeature;
import me.kiriyaga.nami.impl.feature.impl.client.RotationsFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import me.kiriyaga.nami.impl.setting.impl.IntSetting;
import me.kiriyaga.nami.impl.setting.impl.WhitelistSetting;
import me.kiriyaga.nami.util.PredictMovementUtils;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.*;

@RegisterFeature
public class ScaffoldFeature extends Feature {

    public final DoubleSetting range = addSetting(new DoubleSetting("Range", 3.00, 1.0, 6.0));
    private final IntSetting delay = addSetting(new IntSetting("Delay", 0, 0, 5));
    private final IntSetting shiftTicks = addSetting(new IntSetting("ShiftTicks", 1, 1, 8));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    private final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));
    private final BoolSetting multiTask = addSetting(new BoolSetting("MultiTask", false));
    private final BoolSetting simulate = addSetting(new BoolSetting("Simulate", false));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", false));
    public final WhitelistSetting whitelist = addSetting(new WhitelistSetting("WhiteList", false, WhitelistSetting.Type.BLOCK));
    private final BoolSetting singleBlock = addSetting(new BoolSetting("SingleBlock", true));
    private final BoolSetting lookBack = addSetting(new BoolSetting("LookBack", true));
    private final BoolSetting render = addSetting(new BoolSetting("Render", false));

    private int cooldown = 0;
    private BlockPos renderPos = null;

    public ScaffoldFeature() {
        super("Scaffold", "Automatically scaffolds using specified blocks.", FeatureCategory.of("World"));
    }

    @Override
    public void onDisable() {
        cooldown = 0;
        renderPos = null;
    }

    @SubscribeEvent
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) {
            cooldown = 0;
            renderPos = null;
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            renderPos = null;
            return;
        }
        BlockPos[] corners = getPlacements();
        int blocksPlaced = 0;
        int slot = getSlot();
        if (slot == -1) {
            renderPos = null;
            return;
        }

        renderPos = null;
        for (BlockPos pos : corners) {
            BlockPos targetPos = pos.below();

            if (isPlaceable(targetPos) || !isReplaceable(targetPos))
                continue;

            renderPos = targetPos;

            if (placeBlock(targetPos, slot, range.get(), rotate.get(), strictDirection.get(), simulate.get(), swing.get(), this.name, multiTask.get()))
                blocksPlaced++;

            if (blocksPlaced >= shiftTicks.get()) break;
        }

        if (lookBack.get() && INPUT_SERVICE.hasAnyInput() && FEATURE_SERVICE.getStorage().getByClass(RotationsFeature.class).rotation.get() == RotationsFeature.RotationMode.MOTION)
            ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(this.name+"hold", 3, INPUT_SERVICE.getDirection() - 180, 81));

        if (blocksPlaced > 0) cooldown = delay.get();
    }

    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MC.player == null || MC.level == null || renderPos == null || !render.get()) return;

        PoseStack matrices = event.getMatrices();

        ColorFeature colorFeature = FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class);
        Color color = colorFeature.getStyledGlobalColor();

        AABB box = new AABB(renderPos);

        RenderUtil.drawBoxLines(box, color, true, true, 1.5f);

    }

    private int getSlot() {
        int selectedSlot = MC.player.getInventory().getSelectedSlot();

        if (!MC.player.getInventory().getItem(selectedSlot).isEmpty()) {
            Block block = Block.byItem(MC.player.getInventory().getItem(selectedSlot).getItem());
            if (block != Blocks.AIR) {
                Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
                if (!whitelist.get() || whitelist.isWhitelisted(blockId)) {
                    return selectedSlot;
                }
            }
        }

        for (int i = 0; i < 9; i++) {
            if (MC.player.getInventory().getItem(i).isEmpty()) continue;

            Block block = Block.byItem(MC.player.getInventory().getItem(i).getItem());
            if (block == Blocks.AIR) continue;

            Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
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

        BlockPos[] valid = new BlockPos[]{
                new BlockPos((int) Math.floor(minX), y, (int) Math.floor(minZ)),
                new BlockPos((int) Math.floor(minX), y, (int) Math.floor(maxZ)),
                new BlockPos((int) Math.floor(maxX), y, (int) Math.floor(minZ)),
                new BlockPos((int) Math.floor(maxX), y, (int) Math.floor(maxZ))
        };

        if (!singleBlock.get() || valid.length <= 1)
            return valid;

        PredictMovementUtils.PredictedEntity initial = new PredictMovementUtils.PredictedEntity(MC.player.position(), MC.player.getDeltaMovement(), MC.player.getYRot(), MC.player.getXRot(), MC.player.onGround(), MC.player.getEyeHeight());

        PredictMovementUtils.PredictedEntity predicted = PredictMovementUtils.predict(initial, 3, t -> Vec3.ZERO);
        Vec3 eyePos = predicted != null ? predicted.getEyePos() : MC.player.getEyePosition();
        BlockPos closest = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : valid) {
            Vec3 center = Vec3.atCenterOf(pos);
            double dist = center.distanceToSqr(eyePos);

            if (dist < bestDist) {
                bestDist = dist;
                closest = pos;
            }
        }

        return closest != null ? new BlockPos[]{closest} : valid;
    }
}