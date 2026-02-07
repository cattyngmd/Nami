package namidevelopment.kiriyaga.nami.impl.feature.movement.component;

import namidevelopment.kiriyaga.api.event.impl.LedgeClipEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.setting.EnumSetting;
import namidevelopment.kiriyaga.nami.mixin.DuckKeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static namidevelopment.kiriyaga.api.NamiApi.MC;

public class SafeWalkComponent {

    public enum Mode {
        ALWAYS,
        CORNERS,
        LEDGE
    }

    private static final double EDGE_THRESHOLD = 0.55;
    private static final int CHECK_RADIUS = 1;

    public EnumSetting<Mode> mode;

    public void register(Feature feature) {
        mode = feature.addSetting(new EnumSetting<>("Sneak", Mode.CORNERS));
    }

    public void onDisable() {
        setSneakHeld(false);
    }

    public void onTick() {
        if (MC.player == null) return;

        boolean shouldSneak = switch (mode.get()) {
            case ALWAYS -> true;
            case CORNERS -> shouldSneakAtEdges(MC.player);
            case LEDGE -> false;
        };

        setSneakHeld(shouldSneak);
    }

    public void onLedgeClip(LedgeClipEvent event) {
        if (mode.get() != Mode.LEDGE) return;
        if (MC.player == null) return;

        if (!MC.player.isShiftKeyDown()) {
            MC.player.setShiftKeyDown(true);
            event.cancel();
            event.setClipped(true);
        }
    }

    private boolean shouldSneakAtEdges(LocalPlayer player) {
        Vec3 pos = player.position();
        int blockY = (int) Math.floor(pos.y - 0.001);

        if (!player.onGround())
            return false;

        BlockPos basePos = new BlockPos(player.blockPosition().getX(), blockY, player.blockPosition().getZ());

        BlockPos closestBlock = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (int dx = -CHECK_RADIUS; dx <= CHECK_RADIUS; dx++) {
            for (int dz = -CHECK_RADIUS; dz <= CHECK_RADIUS; dz++) {
                BlockPos checkPos = basePos.offset(dx, 0, dz);
                BlockState state = MC.level.getBlockState(checkPos);

                if (state.isAir()) continue;

                double centerX = checkPos.getX() + 0.5;
                double centerZ = checkPos.getZ() + 0.5;
                double distSq = pos.distanceToSqr(centerX, pos.y, centerZ);

                if (distSq < closestDistanceSq) {
                    closestDistanceSq = distSq;
                    closestBlock = checkPos;
                }
            }
        }

        if (closestBlock == null) return true;

        double centerX = closestBlock.getX() + 0.5;
        double centerZ = closestBlock.getZ() + 0.5;
        double dx = pos.x - centerX;
        double dz = pos.z - centerZ;

        boolean nearEdgeX = Math.abs(dx) > EDGE_THRESHOLD;
        boolean nearEdgeZ = Math.abs(dz) > EDGE_THRESHOLD;

        if (!nearEdgeX && !nearEdgeZ) {
            return false;
        }

        int offsetX = 0;
        int offsetZ = 0;

        if (Math.abs(dx) >= Math.abs(dz)) {
            offsetX = dx > 0 ? 1 : -1;
        } else {
            offsetZ = dz > 0 ? 1 : -1;
        }

        BlockPos directionToCheck = closestBlock.offset(offsetX, 0, offsetZ);
        BlockState supportBlock = MC.level.getBlockState(directionToCheck);

        return supportBlock.isAir();
    }

    private void setSneakHeld(boolean held) {
        KeyMapping sneakKey = MC.options.keyShift;
        InputConstants.Key boundKey = ((DuckKeyMapping) sneakKey).getKey();
        int keyCode = boundKey.getValue();

        boolean physicallyPressed = InputConstants.isKeyDown(MC.getWindow(), keyCode);
        sneakKey.setDown(physicallyPressed || held);
    }
}
