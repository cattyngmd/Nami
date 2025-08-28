package me.kiriyaga.nami.feature.module.impl.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.kiriyaga.nami.core.config.model.PrinterSchematic;
import me.kiriyaga.nami.core.rotation.RotationRequest;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.command.impl.PrinterCommand;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
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
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class PrinterModule extends Module {

    private final DoubleSetting range = addSetting(new DoubleSetting("range", 5.0, 1.0, 10.0));
    public final IntSetting delay = addSetting(new IntSetting("delay", 4, 1, 20));
    private final IntSetting shiftTicks = addSetting(new IntSetting("shift ticks", 1, 1, 6));
    private final BoolSetting grim = addSetting(new BoolSetting("grim", false));
    private final BoolSetting rotate = addSetting(new BoolSetting("rotate", true));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation priority", 2, 1, 10));

    private int cooldown = 0;
    private int swapCooldown = 0;
    private BlockPos renderPos = null;

    public PrinterModule() {
        super("printer", "Simplified printer module, it uses air place only.", ModuleCategory.of("world"));
        rotationPriority.setShowCondition(rotate::get);
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

        if (PrinterCommand.getLoadedSchematics().isEmpty()) {
            renderPos = null;
            return;
        }

        if (swapCooldown > 0) swapCooldown--;

        List<TargetBlock> targets = new ArrayList<>();
        for (PrinterSchematic schematic : PrinterCommand.getLoadedSchematics()) {
            String type = schematic.getType();
            JsonArray blocks = schematic.getBlocks();
            for (int i = 0; i < blocks.size(); i++) {
                JsonObject obj = blocks.get(i).getAsJsonObject();
                int bx = obj.get("x").getAsInt();
                int by = obj.get("y").getAsInt();
                int bz = obj.get("z").getAsInt();
                String blockId = obj.get("block").getAsString();

                Block required = Registries.BLOCK.get(Identifier.of(blockId));
                BlockPos worldPos;
                if ("dynamic".equalsIgnoreCase(type)) {
                    if (PrinterCommand.pos1 == null) continue;
                    worldPos = PrinterCommand.pos1.add(bx, by, bz);
                } else {
                    worldPos = new BlockPos(bx, by, bz);
                }

                targets.add(new TargetBlock(worldPos, required));
            }
        }

        if (targets.isEmpty()) {
            renderPos = null;
            return;
        }

        Vec3d playerVec = MC.player.getPos();
        double maxRange = range.get();
        double maxRangeSq = maxRange * maxRange;
        targets.sort(Comparator.comparingDouble(t -> t.pos.getSquaredDistance(MC.player.getX(), MC.player.getY(), MC.player.getZ())));

        boolean placed = false;

        int blocksPlaced = 0;

        for (TargetBlock t : targets) {
            if (blocksPlaced >= shiftTicks.get()) break;

            double distSq = t.pos.getSquaredDistance(MC.player.getX(), MC.player.getY(), MC.player.getZ());
            if (distSq > maxRangeSq) continue;
            if (hasEntity(t.pos)) continue;

            BlockState current = MC.world.getBlockState(t.pos);
            if (!current.isAir() && current.getBlock() == t.required) continue;
            if (!current.isAir()) continue;

            int slot = findSlotForBlock(t.required);
            if (slot == -1) slot = findBlockInHotbar();
            if (slot == -1) {
                renderPos = null;
                continue;
            }

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != slot && swapCooldown <= 0) {
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(slot);
                swapCooldown = delay.get();
                renderPos = t.pos;
                return;
            }

            if (rotate.get()) {
                ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(
                        LiquidFillModule.class.getName(),
                        rotationPriority.get(),
                        (float) getYawToVec(MC.player, Vec3d.of(t.pos)),
                        (float) getPitchToVec(MC.player, Vec3d.of(t.pos))
                ));
            }

            if (!rotate.get() || ROTATION_MANAGER.getRequestHandler().isCompleted(LiquidFillModule.class.getName())) {
                if (grim.get()) {
                    MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                    MC.interactionManager.interactBlock(MC.player, Hand.OFF_HAND, new BlockHitResult(
                            Vec3d.of(t.pos).add(0.5, 0.5, 0.5),
                            Direction.UP,
                            t.pos,
                            false));
                    MC.player.swingHand(Hand.MAIN_HAND, false);
                    MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.OFF_HAND));
                    MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                } else {
                    MC.interactionManager.interactBlock(MC.player, Hand.MAIN_HAND, new BlockHitResult(
                            Vec3d.of(t.pos).add(0.5, 0.5, 0.5),
                            Direction.UP,
                            t.pos,
                            false));
                    MC.player.swingHand(Hand.MAIN_HAND);
                }

                cooldown = delay.get();
                renderPos = t.pos;
                blocksPlaced++;
            }
        }

        if (blocksPlaced == 0) renderPos = null;
    }

    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MC.player == null || MC.world == null) return;

        MatrixStack matrices = event.getMatrices();
        ColorModule colorModule = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
        Color color = colorModule.getStyledGlobalColor();
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 60);

        if (PrinterCommand.getLoadedSchematics().isEmpty()) {
            BlockPos p1 = PrinterCommand.pos1;
            BlockPos p2 = PrinterCommand.pos2;
            if (p1 != null && p2 != null) {
                Box box = new Box(
                        Math.min(p1.getX(), p2.getX()),
                        Math.min(p1.getY(), p2.getY()),
                        Math.min(p1.getZ(), p2.getZ()),
                        Math.max(p1.getX(), p2.getX()) + 1,
                        Math.max(p1.getY(), p2.getY()) + 1,
                        Math.max(p1.getZ(), p2.getZ()) + 1
                );
                RenderUtil.drawBox(matrices, box, fillColor, color, 1.5f, true, true);
            } else if (p1 != null) {
                RenderUtil.drawBox(matrices, new Box(p1), fillColor, color, 1.5f, true, true);
            } else if (p2 != null) {
                RenderUtil.drawBox(matrices, new Box(p2), fillColor, color, 1.5f, true, true);
            }
            return;
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (PrinterSchematic schematic : PrinterCommand.getLoadedSchematics()) {
            JsonArray blocks = schematic.getBlocks();
            for (int i = 0; i < blocks.size(); i++) {
                JsonObject obj = blocks.get(i).getAsJsonObject();
                int bx = obj.get("x").getAsInt();
                int by = obj.get("y").getAsInt();
                int bz = obj.get("z").getAsInt();

                BlockPos worldPos;
                if ("dynamic".equalsIgnoreCase(schematic.getType())) {
                    if (PrinterCommand.pos1 == null) continue;
                    worldPos = PrinterCommand.pos1.add(bx, by, bz);
                } else {
                    worldPos = new BlockPos(bx, by, bz);
                }

                minX = Math.min(minX, worldPos.getX());
                minY = Math.min(minY, worldPos.getY());
                minZ = Math.min(minZ, worldPos.getZ());
                maxX = Math.max(maxX, worldPos.getX());
                maxY = Math.max(maxY, worldPos.getY());
                maxZ = Math.max(maxZ, worldPos.getZ());
            }
        }

        if (minX != Integer.MAX_VALUE) {
            Box bigBox = new Box(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
            RenderUtil.drawBox(matrices, bigBox, fillColor, color, 1.5f, false, true);
        }

        if (renderPos != null) {
            Color highlightFill = new Color(color.getRed(), color.getGreen(), color.getBlue(), 120);
            RenderUtil.drawBox(matrices, new Box(renderPos), highlightFill, color, 2.0f, true, true);
        }
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

    private int findSlotForBlock(Block required) {
        if (required == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block == required) return i;
            }
        }
        return -1;
    }

    private static class TargetBlock {
        final BlockPos pos;
        final Block required;

        TargetBlock(BlockPos pos, Block required) {
            this.pos = pos;
            this.required = required;
        }
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
