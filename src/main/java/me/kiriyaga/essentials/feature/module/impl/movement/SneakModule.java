package me.kiriyaga.essentials.feature.module.impl.movement;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PreTickEvent;
import me.kiriyaga.essentials.event.impl.Render3DEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.mixin.KeyBindingAccessor;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.EnumSetting;
import me.kiriyaga.essentials.util.render.RenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class SneakModule extends Module {

    public enum Mode {
        Always,
        Corners,
        Predictive
    }

    private final Map<BlockPos, Color> checkedBlocks = new HashMap<>();

    private final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("mode", Mode.Always));
    private final BoolSetting render = addSetting(new BoolSetting("render", false));

    private static final double EDGE_THRESHOLD = 0.2;
    private static final int CHECK_RADIUS = 1;

    public SneakModule() {
        super("sneak", "Automatically makes you sneak.", Category.movement);
    }

    @Override
    public void onDisable() {
        setSneakHeld(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdateEvent(PreTickEvent event) {
        ClientPlayerEntity player = MINECRAFT.player;
        if (player == null) return;

        boolean shouldSneak = switch (mode.get()) {
            case Always -> true;
            case Corners -> shouldSneakAtEdges(player);
            case Predictive -> shouldSneakAtEdges(player) && isMovingTowardsEdge(player, 0.003);
        };

        setSneakHeld(shouldSneak);
    }

    private boolean isOnBlockEdgeWithSupport(ClientPlayerEntity player) {
        Vec3d pos = player.getPos();
        int blockY = (int) Math.floor(player.getY() - 0.001);

        double relX = pos.x - Math.floor(pos.x);
        double relZ = pos.z - Math.floor(pos.z);

        int baseX = (int) Math.floor(pos.x);
        int baseZ = (int) Math.floor(pos.z);

        boolean nearEdgeX = relX < EDGE_THRESHOLD || relX > 1.0 - EDGE_THRESHOLD;
        boolean nearEdgeZ = relZ < EDGE_THRESHOLD || relZ > 1.0 - EDGE_THRESHOLD;

        if (!nearEdgeX && !nearEdgeZ) return false;

        int[] xOffsets = {0};
        int[] zOffsets = {0};

        if (relX < EDGE_THRESHOLD) xOffsets = new int[]{0, -1};
        else if (relX > 1.0 - EDGE_THRESHOLD) xOffsets = new int[]{0, 1};

        if (relZ < EDGE_THRESHOLD) zOffsets = new int[]{0, -1};
        else if (relZ > 1.0 - EDGE_THRESHOLD) zOffsets = new int[]{0, 1};

        for (int dx : xOffsets) {
            for (int dz : zOffsets) {
                BlockPos posToCheck = new BlockPos(baseX + dx, blockY, baseZ + dz);
                if (MINECRAFT.world.getBlockState(posToCheck).isAir()) {
                    return false;
                } else {
                    checkedBlocks.putIfAbsent(posToCheck, new Color(255, 255, 0, 60));
                }
            }
        }

        return true;
    }


    private boolean shouldSneakAtEdges(ClientPlayerEntity player) {
        Vec3d pos = player.getPos();
        int blockY = (int) Math.floor(player.getY() - 0.001);

        checkedBlocks.clear();

        if (isOnBlockEdgeWithSupport(player)) {
            return false;
        }

        Map<BlockPos, Color> yellowBlocks = new HashMap<>();

        for (int dx = -CHECK_RADIUS; dx <= CHECK_RADIUS; dx++) {
            for (int dz = -CHECK_RADIUS; dz <= CHECK_RADIUS; dz++) {
                BlockPos checkPos = new BlockPos(
                        (int) Math.floor(pos.x) + dx,
                        blockY,
                        (int) Math.floor(pos.z) + dz
                );

                if (dx == 0 && dz == 0) continue;

                BlockState state = MINECRAFT.world.getBlockState(checkPos);
                if (state.isAir()) continue;

                for (int ndx = -1; ndx <= 1; ndx++) {
                    for (int ndz = -1; ndz <= 1; ndz++) {
                        if (ndx == 0 && ndz == 0) continue;
                        BlockPos neighborPos = checkPos.add(ndx, 0, ndz);
                        BlockState neighborState = MINECRAFT.world.getBlockState(neighborPos);
                        if (neighborState.isAir()) continue;

                        double neighborCenterX = neighborPos.getX() + 0.5;
                        double neighborCenterZ = neighborPos.getZ() + 0.5;

                        double distX = Math.abs(pos.x - neighborCenterX);
                        double distZ = Math.abs(pos.z - neighborCenterZ);

                        if (distX < EDGE_THRESHOLD && distZ < EDGE_THRESHOLD) {
                            yellowBlocks.put(neighborPos, new Color(255, 255, 0, 60));
                        }
                    }
                }
            }
        }

        for (int dx = -CHECK_RADIUS; dx <= CHECK_RADIUS; dx++) {
            for (int dz = -CHECK_RADIUS; dz <= CHECK_RADIUS; dz++) {
                BlockPos checkPos = new BlockPos(
                        (int) Math.floor(pos.x) + dx,
                        blockY,
                        (int) Math.floor(pos.z) + dz
                );

                if (dx == 0 && dz == 0) continue;

                BlockState state = MINECRAFT.world.getBlockState(checkPos);
                if (state.isAir()) continue;

                double centerX = checkPos.getX() + 0.5;
                double centerZ = checkPos.getZ() + 0.5;

                double distX = Math.abs(pos.x - centerX);
                double distZ = Math.abs(pos.z - centerZ);

                if (distX > EDGE_THRESHOLD || distZ > EDGE_THRESHOLD) {
                    boolean hasYellowNeighbor = false;
                    for (int ndx = -1; ndx <= 1; ndx++) {
                        for (int ndz = -1; ndz <= 1; ndz++) {
                            if (ndx == 0 && ndz == 0) continue;
                            BlockPos neighborPos = checkPos.add(ndx, 0, ndz);
                            if (yellowBlocks.containsKey(neighborPos)) {
                                hasYellowNeighbor = true;
                                break;
                            }
                        }
                        if (hasYellowNeighbor) break;
                    }
                    checkedBlocks.put(checkPos, hasYellowNeighbor ? new Color(255, 255, 0, 60) : new Color(0, 255, 0, 60));
                    if (!hasYellowNeighbor) {
                        return true;
                    }
                }
            }
        }

        checkedBlocks.putAll(yellowBlocks);

        return false;
    }


    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MINECRAFT.player == null || MINECRAFT.world == null || !render.get()) return;

        MatrixStack matrices = event.getMatrices();

        Set<BlockPos> renderedPositions = new HashSet<>();
        int maxRenderCount = 25;

        for (Map.Entry<BlockPos, Color> entry : checkedBlocks.entrySet()) {
            if (renderedPositions.size() >= maxRenderCount) break;

            BlockPos pos = entry.getKey();
            BlockPos normalizedPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());

            if (renderedPositions.add(normalizedPos)) {
                Color color = entry.getValue();
                RenderUtil.drawBox(matrices, new Box(normalizedPos), color, color, 1.5, true, true);
            }
        }
    }

    private void setSneakHeld(boolean held) {
        KeyBinding sneakKey = MINECRAFT.options.sneakKey;
        InputUtil.Key boundKey = ((KeyBindingAccessor) sneakKey).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MINECRAFT.getWindow().getHandle(), keyCode);
        sneakKey.setPressed(physicallyPressed || held);
    }

    private boolean isMovingTowardsEdge(ClientPlayerEntity player, double minSpeed) {
        Vec3d velocity = player.getVelocity();
        if (velocity.lengthSquared() < minSpeed * minSpeed) return false;

        Vec3d pos = player.getPos();
        int baseX = (int) Math.floor(pos.x);
        int baseZ = (int) Math.floor(pos.z);

        double relativeX = pos.x - baseX;
        double relativeZ = pos.z - baseZ;

        int offsetX = 0;
        int offsetZ = 0;

        if (relativeX < 0.5 - EDGE_THRESHOLD) offsetX = -1;
        else if (relativeX > 0.5 + EDGE_THRESHOLD) offsetX = 1;

        if (relativeZ < 0.5 - EDGE_THRESHOLD) offsetZ = -1;
        else if (relativeZ > 0.5 + EDGE_THRESHOLD) offsetZ = 1;

        if (offsetX == 0 && offsetZ == 0) return false;

        Vec3d edgeDirection = new Vec3d(offsetX, 0, offsetZ).normalize();
        Vec3d horizontalVelocity = new Vec3d(velocity.x, 0, velocity.z).normalize();

        double dot = horizontalVelocity.dotProduct(edgeDirection);

        return dot > 0.5;
    }
}
