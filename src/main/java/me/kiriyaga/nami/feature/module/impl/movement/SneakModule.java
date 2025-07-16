package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.LedgeClipEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
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

import static me.kiriyaga.nami.Nami.MC;

public class SneakModule extends Module {

    public enum Mode {
        always,
        corners,
        ledge
    }

    private final Map<BlockPos, Color> checkedBlocks = new HashMap<>();

    private final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("mode", Mode.always));
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
        ClientPlayerEntity player = MC.player;
        if (player == null) return;

        boolean shouldSneak = switch (mode.get()) {
            case always -> true;
            case corners -> shouldSneakAtEdges(player);
            case ledge -> false;
        };

        setSneakHeld(shouldSneak);
    }


    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLedgeClip(LedgeClipEvent event) {
        if (mode.get() != Mode.ledge) return;
        assert MC.player != null;
        if (!MC.player.isSneaking()) {
            MC.player.setSneaking(true);
            event.cancel();
            event.setClipped(true);
        }
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
                if (MC.world.getBlockState(posToCheck).isAir()) {
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

                BlockState state = MC.world.getBlockState(checkPos);
                if (state.isAir()) continue;

                for (int ndx = -1; ndx <= 1; ndx++) {
                    for (int ndz = -1; ndz <= 1; ndz++) {
                        if (ndx == 0 && ndz == 0) continue;
                        BlockPos neighborPos = checkPos.add(ndx, 0, ndz);
                        BlockState neighborState = MC.world.getBlockState(neighborPos);
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

                BlockState state = MC.world.getBlockState(checkPos);
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
        if (MC.player == null || MC.world == null || !render.get()) return;

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
        KeyBinding sneakKey = MC.options.sneakKey;
        InputUtil.Key boundKey = ((KeyBindingAccessor) sneakKey).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), keyCode);
        sneakKey.setPressed(physicallyPressed || held);
    }
}