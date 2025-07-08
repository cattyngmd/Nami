package me.kiriyaga.essentials.feature.module.impl.movement;

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
import java.util.Map;

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

    private static final double EDGE_THRESHOLD = 0.25;
    private static final int CHECK_RADIUS = 1;

    public SneakModule() {
        super("sneak", "Automatically makes you sneak.", Category.movement);
    }

    @Override
    public void onDisable() {
        setSneakHeld(false);
    }

    @SubscribeEvent
    public void onUpdateEvent(PreTickEvent event) {
        if (!isEnabled()) return;

        ClientPlayerEntity player = MINECRAFT.player;
        if (player == null) return;

        boolean shouldSneak = switch (mode.get()) {
            case Always -> true;
            case Corners -> shouldSneakAtEdges(player);
            case Predictive -> shouldSneakAtEdges(player) && isMovingTowardsEdge(player, 0.003);
        };

        setSneakHeld(shouldSneak);
    }

    private boolean shouldSneakAtEdges(ClientPlayerEntity player) {
        Vec3d pos = player.getPos();
        int blockY = (int) Math.floor(player.getY() - 0.001);

        checkedBlocks.clear();


        for (int dx = -CHECK_RADIUS; dx <= CHECK_RADIUS; dx++) {
            for (int dz = -CHECK_RADIUS; dz <= CHECK_RADIUS; dz++) {
                BlockPos checkPos = new BlockPos(
                        (int) Math.floor(pos.x) + dx,
                        blockY,
                        (int) Math.floor(pos.z) + dz
                );

                checkedBlocks.put(checkPos, new Color(0, 255, 0, 60));

                BlockState state = MINECRAFT.world.getBlockState(checkPos);
                if (state.isAir()) continue;

                double centerX = checkPos.getX() + 0.5;
                double centerZ = checkPos.getZ() + 0.5;

                double distX = Math.abs(pos.x - centerX);
                double distZ = Math.abs(pos.z - centerZ);

                if (distX > EDGE_THRESHOLD || distZ > EDGE_THRESHOLD) {
                    if (!isInternalIntersection(pos, checkPos)) {
                            return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isInternalIntersection(Vec3d pos, BlockPos blockPos) {

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos neighborPos = blockPos.add(dx, 0, dz);
                BlockState neighborState = MINECRAFT.world.getBlockState(neighborPos);
                if (neighborState.isAir()) continue;

                checkedBlocks.put(neighborPos, new Color(255, 255, 0, 60));

                double neighborCenterX = neighborPos.getX() + 0.5;
                double neighborCenterZ = neighborPos.getZ() + 0.5;

                double distX = Math.abs(pos.x - neighborCenterX);
                double distZ = Math.abs(pos.z - neighborCenterZ);

                if (distX < EDGE_THRESHOLD && distZ < EDGE_THRESHOLD) {
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MINECRAFT.player == null || MINECRAFT.world == null || !render.get()) return;

        MatrixStack matrices = event.getMatrices();

        for (Map.Entry<BlockPos, Color> entry : checkedBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            Color color = entry.getValue();

            RenderUtil.drawBox(matrices, new Box(pos), color, color, 1.5, true, true);
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
