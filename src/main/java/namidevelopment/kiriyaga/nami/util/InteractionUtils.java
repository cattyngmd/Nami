package namidevelopment.kiriyaga.nami.util;

import namidevelopment.kiriyaga.nami.api.rotation.model.RotationRequest;
import namidevelopment.kiriyaga.nami.impl.feature.impl.client.RotationsFeature;
import namidevelopment.kiriyaga.nami.mixin.DuckMultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.nami.util.PacketUtils.sendSequencedPacket;
import static namidevelopment.kiriyaga.nami.util.RotationUtils.*;
import static net.minecraft.world.InteractionHand.MAIN_HAND;

public class InteractionUtils {

    private static BlockPos currentBreakingBlock = null;

    public static boolean interactWithEntity(Entity entity, double range, boolean swing, boolean rotate, String rotationId) {
        if (MC.player == null || MC.gameMode == null || entity == null) return false;

        Vec3 eyePos = MC.player.getEyePosition(1.0f);
        Vec3 closestPoint = getClosestPointToEye(eyePos, entity.getBoundingBox());
        float idealYaw = (float) getYawToVec(MC.player, closestPoint);
        float idealPitch = (float) getPitchToVec(MC.player, closestPoint);

        boolean insideBox = entity.getBoundingBox().contains(MC.player.getEyePosition());
        EntityHitResult hitResult = raycastTarget(MC.player, entity, range, idealYaw, idealPitch);
        if (!insideBox && hitResult == null) {
            return false;
        }

        if (rotate)
            ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(rotationId, 4, idealYaw, idealPitch));

        boolean completed = !rotate || ROTATION_SERVICE.getRequestHandler().isCompleted(rotationId);

        if (!completed)
            return false;

        MC.gameMode.interactAt(MC.player, entity, hitResult, MAIN_HAND);
        MC.gameMode.interact(MC.player, entity, MAIN_HAND);

        if (swing)
            MC.player.swing(MAIN_HAND);

        return true;
    }

    public static void startUsingItem() {
        startUsingItem(MAIN_HAND);
    }

    public static void startUsingItem(InteractionHand hand) {
        if (!MC.player.isUsingItem())
            MC.player.startUsingItem(hand);
    }

    public static void stopUsingItem() {
        if (MC.player.isUsingItem())
            MC.player.releaseUsingItem();
    }

    // TODO: figure out how to place on interactable blocks without manually sneaking

    public static boolean placeBlock(BlockPos pos, int slot, double range, boolean rotate, boolean strictDirection, boolean simulate, boolean swing, String rotationId, boolean multiTask) {
        if (!MC.level.getBlockState(pos).canBeReplaced())
            return false;

        if (!multiTask && MC.player.isUsingItem())
            return false;

        Direction direction = getBlockPlaceDir(pos);
        if (direction == null) {
            return false;
        }
        BlockPos neighbor = pos.relative(direction.getOpposite());
        Direction clickFace = direction;

        Vec3 playerPos = MC.player.position();
        double offX = playerPos.x - Math.floor(playerPos.x);
        double offY = playerPos.y - Math.floor(playerPos.y);
        double offZ = playerPos.z - Math.floor(playerPos.z);
        offX = Mth.clamp(offX, 0.2, 0.8);
        offY = Mth.clamp(offY, 0.2, 0.8);
        offZ = Mth.clamp(offZ, 0.2, 0.8);
        Vec3 hitVec = Vec3.atCenterOf(neighbor);

        switch (clickFace) { //todo: refactor this
            case UP, DOWN -> hitVec = hitVec.add(
                    offX - 0.5,
                    clickFace == Direction.UP ? 0.5 : -0.5,
                    offZ - 0.5
            );

            case NORTH, SOUTH -> hitVec = hitVec.add(
                    offX - 0.5,
                    offY - 0.5,
                    clickFace == Direction.SOUTH ? 0.5 : -0.5
            );

            case EAST, WEST -> hitVec = hitVec.add(
                    clickFace == Direction.EAST ? 0.5 : -0.5,
                    offY - 0.5,
                    offZ - 0.5
            );
        }

        // Simplified grim v2 PlacePosition check
        // we do not use all possible eye positions because its just unnecessary
        if (strictDirection) { // todo something while phased
            Vec3 eyePos = MC.player.getEyePosition();

            boolean flag = switch (clickFace) { // https://github.com/GrimAnticheat/Grim/blob/fb926ab0fbca081ad765389c541880a4a435fabb/common/src/main/java/ac/grim/grimac/checks/impl/scaffolding/PositionPlace.java#L49
                case NORTH -> eyePos.z <= neighbor.getZ() + 1e-3;
                case SOUTH -> eyePos.z >= neighbor.getZ() + 1 - 1e-3;
                case WEST  -> eyePos.x <= neighbor.getX() + 1e-3;
                case EAST  -> eyePos.x >= neighbor.getX() + 1 - 1e-3;
                case DOWN  -> eyePos.y <= neighbor.getY() + 1e-3;
                case UP    -> eyePos.y >= neighbor.getY() + 1 - 1e-3;
                default -> false;
            };

            if (!flag) {
                return false;
            }
        }

        Vec3 eyePos = MC.player.getEyePosition();
            AABB blockBox = new AABB(pos);
            Vec3 point = RotationUtils.getClosestPointToEye(eyePos, blockBox);
            float idealYaw = (float) getYawToVec(MC.player, point);
            float idealPitch = (float) getPitchToVec(MC.player, point);

            if (RotationUtils.raycastAABBFromPlayer(MC.player, blockBox, range, idealYaw, idealPitch) == null) {
                return false;
            }

        BlockHitResult hitResult = new BlockHitResult(hitVec, clickFace, neighbor, false);
        boolean canPlace = true;

        if (rotate) {
            float yaw = (float) getYawToVec(MC.player, neighbor.getCenter());
            float pitch = (float) getPitchToVec(MC.player, neighbor.getCenter());

         //   if (getDefaultRotationMode() == RotationFeature.RotationMode.SILENT)
                ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(rotationId, 8, yaw, pitch));
           // else
             //   ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(rotationId, 8, MC.player, hitVec));

            //canPlace = ROTATION_SERVICE.getRequestHandler().isCompleted(rotationId);

            // for some reason grim checks if you look at block, you gonna place, not on a block you click (i see logic here but still)
            AABB b = new AABB(neighbor);
            boolean insideBox = b.contains(MC.player.getEyePosition());

            EntityHitResult serverCheck = raycastAABBFromPlayer(
                    MC.player,
                    b,
                    range,
                    ROTATION_SERVICE.getStateHandler().getServerYaw(),
                    ROTATION_SERVICE.getStateHandler().getServerPitch()
            );


            canPlace = insideBox || serverCheck != null;
        }

        boolean result = false;
        if (canPlace) {
            int prev = MC.player.getInventory().getSelectedSlot();
            INVENTORY_SERVICE.getSlotHandler().attemptSwitch(slot);

            if (simulate)
                MC.gameMode.useItemOn(MC.player, MAIN_HAND, hitResult);
            else
                sendSequencedPacket(id -> new ServerboundUseItemOnPacket(MAIN_HAND, hitResult, id));

            if (swing)
                MC.player.swing(MAIN_HAND);

            result = true;

            INVENTORY_SERVICE.getSlotHandler().attemptSwitch(prev);
        }

        return result;
    }

    public static boolean interactBlockAt(BlockPos pos, int slot, double range, boolean rotate, boolean strictDirection, boolean simulate, boolean swing, String rotationId) {
        Vec3 eyePos = MC.player.getEyePosition();
        Vec3 hitVec = Vec3.atCenterOf(pos);

        Direction clickFace = getBlockInteractDir(pos);

        if (strictDirection) {
            boolean flag = switch (clickFace) {
                case NORTH -> eyePos.z <= pos.getZ() + 1e-3;
                case SOUTH -> eyePos.z >= pos.getZ() + 1 - 1e-3;
                case WEST  -> eyePos.x <= pos.getX() + 1e-3;
                case EAST  -> eyePos.x >= pos.getX() + 1 - 1e-3;
                case DOWN  -> eyePos.y <= pos.getY() + 1e-3;
                case UP    -> eyePos.y >= pos.getY() + 1 - 1e-3;
            };
            if (!flag) {
             //   CHAT_SERVICE.sendRaw("interactBlockAt: failed strictDirection check");
                return false;
            }
        }

            AABB blockBox = new AABB(pos);
            Vec3 point = RotationUtils.getClosestPointToEye(eyePos, blockBox);
            float idealYaw = (float) getYawToVec(MC.player, point);
            float idealPitch = (float) getPitchToVec(MC.player, point);

            if (RotationUtils.raycastAABBFromPlayer(MC.player, blockBox, range, idealYaw, idealPitch) == null) {
                return false;
            }

        BlockHitResult hit = new BlockHitResult(hitVec, clickFace, pos, false);

        boolean canInteract = true;

        if (rotate) {
            float yaw = (float) getYawToVec(MC.player, pos.getCenter());
            float pitch = (float) getPitchToVec(MC.player, pos.getCenter());

            ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(rotationId, 8, yaw, pitch));
            // else
            //   ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(rotationId, 8, MC.player, hitVec));

            //canPlace = ROTATION_SERVICE.getRequestHandler().isCompleted(rotationId);

            // for some reason grim checks if you look at block, you gonna place, not on a block you click (i see logic here but still)
            AABB b = new AABB(pos);
            boolean insideBox = b.contains(MC.player.getEyePosition());

            EntityHitResult serverCheck = raycastAABBFromPlayer(
                    MC.player,
                    b,
                    range,
                    ROTATION_SERVICE.getStateHandler().getServerYaw(),
                    ROTATION_SERVICE.getStateHandler().getServerPitch()
            );


            canInteract = insideBox || serverCheck != null;
        }

        if (!canInteract) {
          //  CHAT_SERVICE.sendRaw("interactBlockAt: rotation incomplete");
            return false;
        }

        int prev = MC.player.getInventory().getSelectedSlot();
        INVENTORY_SERVICE.getSlotHandler().attemptSwitch(slot);

        if (simulate)
            MC.gameMode.useItemOn(MC.player, MAIN_HAND, hit);
        else
            sendSequencedPacket(id -> new ServerboundUseItemOnPacket(MAIN_HAND, hit, id));

        if (swing)
            MC.player.swing(MAIN_HAND);

        INVENTORY_SERVICE.getSlotHandler().attemptSwitch(prev);

        //CHAT_SERVICE.sendRaw("interactBlockAt: success");
        return true;
    }

    public static Direction getBlockInteractDir(BlockPos blockPos) {
        Vec3 playerPos = MC.player.getEyePosition();
        Vec3 blockCenter = Vec3.atCenterOf(blockPos);

        double dx = playerPos.x - blockCenter.x;
        double dy = playerPos.y - blockCenter.y;
        double dz = playerPos.z - blockCenter.z;

        double absX = Math.abs(dx);
        double absY = Math.abs(dy);
        double absZ = Math.abs(dz);

        if (absX >= absY && absX >= absZ) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else if (absY >= absX && absY >= absZ) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        } else {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }


    public static Direction getBlockPlaceDir(BlockPos blockPos) {
        for (final Direction direction : Direction.values()) {
            final BlockState state = MC.level.getBlockState(blockPos.relative(direction));
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                continue;
            }

            Direction opposite = direction.getOpposite();

            return opposite;
        }
        return null;
    }

    private static RotationsFeature.RotationMode getDefaultRotationMode() {
        RotationsFeature Feature = FEATURE_SERVICE.getStorage().getByClass(RotationsFeature.class);
        return Feature != null ? Feature.rotation.get() : RotationsFeature.RotationMode.MOTION;
    }

    public static void airPlace(BlockHitResult target, boolean grim, boolean swing) {
        if (grim) {
            MC.getConnection().send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));

            MC.gameMode.useItemOn(MC.player, InteractionHand.OFF_HAND, target);
            if (swing)
                MC.player.swing(InteractionHand.MAIN_HAND, false);

            MC.getConnection().send(new ServerboundSwingPacket(InteractionHand.OFF_HAND));
            MC.getConnection().send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
        } else {
            MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, target);
            if (swing)
                MC.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    public static boolean breakBlock(BlockPos pos, double range, boolean rotate, boolean swing, boolean grim, boolean strictDirection, String rotationId) {
        if (MC.player == null || MC.gameMode == null)
            return false;

        //CHAT_SERVICE.sendRaw(((ClientPlayerInteractionSERVICEAccessor) MC.interactionSERVICE).getBlockBreakingCooldown()+"");

        if (isBlockAirOrFluid(pos)) {
            if (currentBreakingBlock != null && currentBreakingBlock.equals(pos)) {
                currentBreakingBlock = null;
            }
            return false;
        }

        Vec3 eyePos = MC.player.getEyePosition();
        AABB blockBox = new AABB(pos);
        Vec3 lookDir = getClosestPointToEye(eyePos, blockBox).subtract(eyePos).normalize();
        Vec3 reachEnd = eyePos.add(lookDir.scale(range));

        if (blockBox.clip(eyePos, reachEnd).isEmpty())
            return false;

        double dx = eyePos.x - blockBox.getCenter().x;
        double dy = eyePos.y - blockBox.getCenter().y;
        double dz = eyePos.z - blockBox.getCenter().z;
        double absX = Math.abs(dx);
        double absY = Math.abs(dy);
        double absZ = Math.abs(dz);

        Direction direction = Direction.UP;

        if (strictDirection) {

            if (absY > absX && absY > absZ) direction = dy > 0 ? Direction.UP : Direction.DOWN;
            else if (absX > absZ) direction = dx > 0 ? Direction.EAST : Direction.WEST;
            else direction = dz > 0 ? Direction.SOUTH : Direction.NORTH;

            boolean flag = switch (direction) {
                case NORTH -> eyePos.z <= pos.getZ() + 1e-3;
                case SOUTH -> eyePos.z >= pos.getZ() + 1 - 1e-3;
                case WEST  -> eyePos.x <= pos.getX() + 1e-3;
                case EAST  -> eyePos.x >= pos.getX() + 1 - 1e-3;
                case DOWN  -> eyePos.y <= pos.getY() + 1e-3;
                case UP    -> eyePos.y >= pos.getY() + 1 - 1e-3;
                default -> false;
            };

            if (!flag) {
                return false;
            }
        }


        if (rotate) {
            Vec3 center = Vec3.atCenterOf(pos).add(
                    direction.getStepX() * 0.5,
                    direction.getStepY() * 0.5,
                    direction.getStepZ() * 0.5
            );

            ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(
                    rotationId,
                    3,
                    (float) getYawToVec(MC.player, center),
                    (float) getPitchToVec(MC.player, center)
            ));

            if (!ROTATION_SERVICE.getRequestHandler().isCompleted(rotationId)) {
                return false;
            }
        }

        boolean success = MC.gameMode.continueDestroyBlock(pos, direction);
        if (swing)
            MC.player.swing(MAIN_HAND);

        if (grim && ((DuckMultiPlayerGameMode) MC.gameMode).getDestroyDelay() != 0) // https://github.com/GrimAnticheat/Grim/blob/def21633e2bfa52e2dd4afdf91aec3c0ec6d14e7/common/src/main/java/ac/grim/grimac/checks/impl/breaking/FastBreak.java#L28
            return false;

        if (isBlockAirOrFluid(pos)) {  // somehow it happens https://github.com/GrimAnticheat/Grim/blob/def21633e2bfa52e2dd4afdf91aec3c0ec6d14e7/common/src/main/java/ac/grim/grimac/checks/impl/breaking/AirLiquidBreak.java#L18
            currentBreakingBlock = null;
            return false;
        }

        if (currentBreakingBlock == null || !currentBreakingBlock.equals(pos)) {
            currentBreakingBlock = pos;
            MC.gameMode.startDestroyBlock(pos, direction);
        } else {
            if (!success) {
                currentBreakingBlock = null;
                return false;
            }
        }

        return true;
    }

    private static boolean isBlockAirOrFluid(BlockPos pos) {
        if (MC.level.getBlockState(pos).isAir()) {
            return true;
        }
        FluidState fluidState = MC.level.getFluidState(pos);
        return !fluidState.isEmpty();
    }

    public static boolean isPlaceable(BlockPos pos) {
        return isPlaceable(pos, 10);
    }

    public static boolean isPlaceable(BlockPos pos, int distance) {
        AABB blockBox = new AABB(pos);
        for (Entity entity : MC.level.entitiesForRendering()) {
            if (entity.distanceToSqr(MC.player) > distance) continue;
            if (entity instanceof EndCrystal) continue;
            if (entity instanceof ItemEntity) continue;
            if (entity instanceof Arrow) continue;

            if (entity.getBoundingBox().intersects(blockBox)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isReplaceable(BlockPos pos) {
        return MC.level.getBlockState(pos).canBeReplaced();
    }

    public static boolean isBed(Block block) {
        return block instanceof BedBlock;
    }


    private static Vec3 getLookVectorFromYawPitch(float yaw, float pitch) {
        float f = (float) Math.cos(-yaw * 0.017453292F - Math.PI);
        float g = (float) Math.sin(-yaw * 0.017453292F - Math.PI);
        float h = - (float) Math.cos(-pitch * 0.017453292F);
        float i = (float) Math.sin(-pitch * 0.017453292F);
        return new Vec3(g * h, i, f * h);
    }
}
