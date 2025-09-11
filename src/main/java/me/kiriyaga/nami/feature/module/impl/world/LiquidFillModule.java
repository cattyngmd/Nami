package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.core.rotation.RotationRequest;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.*;
import java.util.List;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class LiquidFillModule extends Module {

    public enum LiquidType {
        WATER, LAVA, BOTH
    }

    private final DoubleSetting range = addSetting(new DoubleSetting("range", 5.0, 1.0, 10.0));
    public final IntSetting delay = addSetting(new IntSetting("delay", 4, 1, 10));
    private final BoolSetting grim = addSetting(new BoolSetting("grim", false));
    private final EnumSetting<LiquidType> liquidType = addSetting(new EnumSetting<>("liquid", LiquidType.BOTH));
    private final BoolSetting rotate = addSetting(new BoolSetting("rotate", true));

    private int cooldown = 0;
    private BlockPos renderPos = null;
    private int swapCooldown = 0;

    public LiquidFillModule() {
        super("liquid fill", "Automatically fills nearby liquids with blocks.", ModuleCategory.of("world"), "liquidfill");
    }

    @Override
    public void onDisable() {
        cooldown = 0;
        swapCooldown = 0;
        renderPos = null;
    }

    @SubscribeEvent
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null || MC.interactionManager == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        int blockSlot = findBlockInHotbar();
        if (blockSlot == -1) {
            renderPos = null;
            return;
        }

        int currentSlot = MC.player.getInventory().getSelectedSlot();
        if (currentSlot != blockSlot && swapCooldown <= 0) {
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(blockSlot);
            swapCooldown = delay.get();
            return;
        }

        if (swapCooldown > 0) swapCooldown--;

        int r = (int) Math.ceil(range.get());
        BlockPos playerPos = MC.player.getBlockPos();

        List<BlockPos> positions = new ArrayList<>();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    positions.add(playerPos.add(x, y, z));
                }
            }
        }

        Vec3d playerVec = Vec3d.of(playerPos); // fucking why i need this
        positions.sort(Comparator.comparingDouble(pos -> Vec3d.of(pos).squaredDistanceTo(playerVec)));

        boolean placed = false;

        for (BlockPos pos : positions) {
            BlockState state = MC.world.getBlockState(pos);
            if (hasEntity(pos)) continue;

            boolean shouldPlace = switch (liquidType.get()) {
                case WATER -> state.getBlock() == Blocks.WATER;
                case LAVA -> state.getBlock() == Blocks.LAVA;
                case BOTH -> state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA;
            };

            if (!shouldPlace) continue;

            renderPos = pos;

            if (rotate.get()) {
                ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(
                        LiquidFillModule.class.getName(),
                        3,
                        (float) getYawToVec(MC.player, Vec3d.of(pos)),
                        (float) getPitchToVec(MC.player, Vec3d.of(pos))
                ));
            }

            if (!rotate.get() || ROTATION_MANAGER.getRequestHandler().isCompleted(LiquidFillModule.class.getName())) {
                if (grim.get()) {
                    MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                    MC.interactionManager.interactBlock(MC.player, Hand.OFF_HAND, new BlockHitResult(
                            Vec3d.of(pos).add(0.5,0.5,0.5),
                            Direction.UP,
                            pos,
                            false));
                    MC.player.swingHand(Hand.MAIN_HAND, false);
                    MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.OFF_HAND));
                    MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                } else {
                    MC.interactionManager.interactBlock(MC.player, Hand.MAIN_HAND, new BlockHitResult(
                            Vec3d.of(pos).add(0.5,0.5,0.5),
                            Direction.UP,
                            pos,
                            false));
                    MC.player.swingHand(Hand.MAIN_HAND);
                }

                cooldown = delay.get();
                placed = true;
                break;
            }
        }

        if (!placed) renderPos = null;
    }

    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MC.player == null || MC.world == null || renderPos == null) return;

        MatrixStack matrices = event.getMatrices();
        ColorModule colorModule = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
        Color color = colorModule.getStyledGlobalColor();
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 60);
        Box box = new Box(renderPos);
        RenderUtil.drawBox(matrices, box, fillColor, color, 1.5f, true, true);
    }

    private boolean hasEntity(BlockPos pos) {
        for (Entity entity : MC.world.getEntities()) {
            if (entity.getBoundingBox().intersects(new Box(pos))) return true;
        }
        return false;
    }

    private int findBlockInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block != Blocks.AIR && block.getDefaultState().isOpaqueFullCube()) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static Vec3d getEntityCenter(Entity entity) {
        Box box = entity.getBoundingBox();
        double centerX = box.minX + (box.getLengthX() / 2);
        double centerY = box.minY + (box.getLengthY() / 2);
        double centerZ = box.minZ + (box.getLengthZ() / 2);
        return new Vec3d(centerX, centerY, centerZ);
    }

    private static int getYawToVec(Entity from, Vec3d to) {
        double dx = to.x - from.getX();
        double dz = to.z - from.getZ();
        return wrapDegrees((int) Math.round(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
    }

    private static int getPitchToVec(Entity from, Vec3d to) {
        Vec3d eyePos = from.getEyePos();
        double dx = to.x - eyePos.x;
        double dy = to.y - eyePos.y;
        double dz = to.z - eyePos.z;
        return (int) Math.round(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
    }

    private static int wrapDegrees(int angle) {
        angle %= 360;
        if (angle >= 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
}