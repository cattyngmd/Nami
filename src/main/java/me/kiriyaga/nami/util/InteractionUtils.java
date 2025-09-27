package me.kiriyaga.nami.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.PacketUtils.sendSequencedPacket;
import static net.minecraft.util.Hand.MAIN_HAND;

public class InteractionUtils {

    public static boolean interactWithEntity(Entity entity, Vec3d hitVec, boolean swing) {
        if (MC.player == null || MC.interactionManager == null) return false;

        EntityHitResult hitResult = new EntityHitResult(entity, hitVec);
        ClientPlayerInteractionManager im = MC.interactionManager;

        im.interactEntityAtLocation(MC.player, entity, hitResult, MAIN_HAND);
        im.interactEntity(MC.player, entity, MAIN_HAND);

        if (swing)
            MC.player.swingHand(MAIN_HAND);

        return true;
    }

    public static void startUsingItem() {
        startUsingItem(MAIN_HAND);
    }

    public static void startUsingItem(Hand hand) {
        if (!MC.player.isUsingItem())
            MC.player.setCurrentHand(hand);
    }

    public static void stopUsingItem() {
        if (MC.player.isUsingItem())
            MC.player.stopUsingItem();
    }

    // TODO: figure out how to place on interactable blocks without manually sneaking

    public static boolean placeBlock(BlockPos pos,int slot, boolean swing, boolean rotate) {
        Direction direction = getDirection(pos);
        if (direction == null)
            return false;

        Vec3d hitVec = pos.toCenterPos().add(new Vec3d(direction.getUnitVector()).multiply(0.5));
        BlockHitResult hitResult = new BlockHitResult(hitVec, direction, pos, false);

        boolean canPlace = true;

        if (rotate) {
            String rotationId = pos.toString();
            float yaw = (float) RotationUtils.getYawToVec(MC.player, hitVec);
            float pitch = (float) RotationUtils.getPitchToVec(MC.player, hitVec);

            ROTATION_MANAGER.getRequestHandler().submit(
                    new me.kiriyaga.nami.core.rotation.model.RotationRequest(
                            rotationId,
                            10,
                            yaw,
                            pitch
                    )
            );

            canPlace = ROTATION_MANAGER.getRequestHandler().isCompleted(rotationId);
        }


        boolean result = false;
        if (canPlace) {
            int prev = MC.player.getInventory().getSelectedSlot();
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(slot);

            sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(MAIN_HAND, hitResult, id));
            if (swing)
                MC.player.swingHand(MAIN_HAND);
            result = true;

            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(prev);
        }

        return result;
    }

    public static Direction getDirection(BlockPos blockPos) { // https://github.com/GrimAnticheat/Grim/blob/293f6ea8b1b99c1ca61bbfa5507d6ee637d69276/common/src/main/java/ac/grim/grimac/checks/impl/scaffolding/AirLiquidPlace.java#L58
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = blockPos.offset(direction);
            BlockState state = MC.world.getBlockState(neighbor);

            if (state.isAir() || !state.getFluidState().isEmpty()) continue;

            return direction;
        }
        return null;
    }
}
