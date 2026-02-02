package namidevelopment.kiriyaga.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

import java.util.function.Predicate;

import static namidevelopment.kiriyaga.api.NamiApi.*;
import static namidevelopment.kiriyaga.api.util.PacketUtils.sendSequencedPacket;
import static namidevelopment.kiriyaga.api.util.RotationUtils.*;
import static net.minecraft.world.InteractionHand.MAIN_HAND;
import static net.minecraft.world.InteractionHand.OFF_HAND;

public class InteractionUtils {

    private static BlockPos currentBreakingBlock = null;

    public static boolean interactWithEntity(Entity entity, double range, boolean swing, boolean rotate, String rotationId) {
        if (API_MC.player == null || API_MC.gameMode == null || entity == null) return false;

        Vec3 eyePos = API_MC.player.getEyePosition(1.0f);
        Vec3 closestPoint = getClosestPointToEye(eyePos, entity.getBoundingBox());
        float idealYaw = (float) getYawToVec(API_MC.player, closestPoint);
        float idealPitch = (float) getPitchToVec(API_MC.player, closestPoint);

        boolean insideBox = entity.getBoundingBox().contains(API_MC.player.getEyePosition());
        EntityHitResult hitResult = raycastTarget(API_MC.player, entity, range, idealYaw, idealPitch);
        if (!insideBox && hitResult == null) {
            return false;
        }

        if (rotate)
            ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(rotationId, 4, idealYaw, idealPitch));

        boolean completed = !rotate || ROTATION_SERVICE.getRequestHandler().isCompleted(rotationId);

        if (!completed)
            return false;

        API_MC.gameMode.interactAt(API_MC.player, entity, hitResult, MAIN_HAND);
        API_MC.gameMode.interact(API_MC.player, entity, MAIN_HAND);

        if (swing)
            API_MC.player.swing(MAIN_HAND);

        return true;
    }

    public static void startUsingItem() {
        startUsingItem(MAIN_HAND);
    }

    public static void startUsingItem(InteractionHand hand) {
        if (!API_MC.player.isUsingItem())
            API_MC.player.startUsingItem(hand);
    }

    public static void stopUsingItem() {
        if (API_MC.player.isUsingItem())
            API_MC.player.releaseUsingItem();
    }

    // TODO: figure out how to place on interactable blocks without manually sneaking

    public static boolean placeBlock(BlockPos pos, Item item, boolean swapBack, double range, boolean rotate, boolean strictDirection, boolean simulate, boolean swing, String rotationId, boolean multiTask) {
        if (!API_MC.level.getBlockState(pos).canBeReplaced())
            return false;

        if (!multiTask && API_MC.player.isUsingItem())
            return false;

        boolean isOffhand = false;
        if (API_MC.player.getOffhandItem().is(item))
            isOffhand = true;

        int slot = findHotbarItem(stack -> stack.is(item));
        if (slot == -1 && !isOffhand)
            return false;

        Direction direction = getBlockPlaceDir(pos);
        if (direction == null) {
            return false;
        }
        BlockPos neighbor = pos.relative(direction.getOpposite());
        Direction clickFace = direction;

        Vec3 playerPos = API_MC.player.position();
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
            Vec3 eyePos = API_MC.player.getEyePosition();

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

        Vec3 eyePos = API_MC.player.getEyePosition();
            AABB blockBox = new AABB(pos);
            Vec3 point = RotationUtils.getClosestPointToEye(eyePos, blockBox);
            float idealYaw = (float) getYawToVec(API_MC.player, point);
            float idealPitch = (float) getPitchToVec(API_MC.player, point);

            if (RotationUtils.raycastAABBFromPlayer(API_MC.player, blockBox, range, idealYaw, idealPitch) == null) {
                return false;
            }

        BlockHitResult hitResult = new BlockHitResult(hitVec, clickFace, neighbor, false);
        boolean canPlace = true;

        if (rotate) {
            float yaw = (float) getYawToVec(API_MC.player, neighbor.getCenter());
            float pitch = (float) getPitchToVec(API_MC.player, neighbor.getCenter());

         //   if (getDefaultRotationMode() == RotationFeature.RotationMode.SILENT)
                ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(rotationId, 8, yaw, pitch));
           // else
             //   ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(rotationId, 8, MC.player, hitVec));

            //canPlace = ROTATION_SERVICE.getRequestHandler().isCompleted(rotationId);

            // for some reason grim checks if you look at block, you gonna place, not on a block you click (i see logic here but still)
            AABB b = new AABB(neighbor);
            boolean insideBox = b.contains(API_MC.player.getEyePosition());

            EntityHitResult serverCheck = raycastAABBFromPlayer(
                    API_MC.player,
                    b,
                    range,
                    ROTATION_SERVICE.getStateHandler().getServerYaw(),
                    ROTATION_SERVICE.getStateHandler().getServerPitch()
            );


            canPlace = insideBox || serverCheck != null;
        }

        boolean result = false;
        if (canPlace) {

            if (!isOffhand) {
                int prev = API_MC.player.getInventory().getSelectedSlot();
                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(slot);

                if (simulate)
                    API_MC.gameMode.useItemOn(API_MC.player, MAIN_HAND, hitResult);
                else
                    sendSequencedPacket(id -> new ServerboundUseItemOnPacket(MAIN_HAND, hitResult, id));

                if (swing)
                    API_MC.player.swing(MAIN_HAND);

                result = true;

                if (swapBack)
                    INVENTORY_SERVICE.getSlotHandler().attemptSwitch(prev);
            }
            else {
                if (simulate)
                    API_MC.gameMode.useItemOn(API_MC.player, OFF_HAND, hitResult);
                else
                    sendSequencedPacket(id -> new ServerboundUseItemOnPacket(OFF_HAND, hitResult, id));

                if (swing)
                    API_MC.player.swing(OFF_HAND);

                result = true;
            }
        }

        return result;
    }

    public static boolean interactBlockAt(BlockPos pos, Item item, boolean swapBack, boolean multiTask, double range, boolean rotate, boolean strictDirection, boolean simulate, boolean swing, String rotationId) {
        Vec3 eyePos = API_MC.player.getEyePosition();
        Vec3 hitVec = Vec3.atCenterOf(pos);

        Direction clickFace = getBlockInteractDir(pos);

        if (!multiTask && API_MC.player.isUsingItem())
            return false;

        boolean isOffhand = false;
        if (API_MC.player.getOffhandItem().is(item))
            isOffhand = true;

        int slot = findHotbarItem(stack -> stack.is(item));
        if (slot == -1 && !isOffhand)
            return false;

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
            float idealYaw = (float) getYawToVec(API_MC.player, point);
            float idealPitch = (float) getPitchToVec(API_MC.player, point);

            if (RotationUtils.raycastAABBFromPlayer(API_MC.player, blockBox, range, idealYaw, idealPitch) == null) {
                return false;
            }

        BlockHitResult hit = new BlockHitResult(hitVec, clickFace, pos, false);

        boolean canInteract = true;

        if (rotate) {
            float yaw = (float) getYawToVec(API_MC.player, pos.getCenter());
            float pitch = (float) getPitchToVec(API_MC.player, pos.getCenter());

            ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(rotationId, 8, yaw, pitch));
            // else
            //   ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(rotationId, 8, MC.player, hitVec));

            //canPlace = ROTATION_SERVICE.getRequestHandler().isCompleted(rotationId);

            // for some reason grim checks if you look at block, you gonna place, not on a block you click (i see logic here but still)
            AABB b = new AABB(pos);
            boolean insideBox = b.contains(API_MC.player.getEyePosition());

            EntityHitResult serverCheck = raycastAABBFromPlayer(
                    API_MC.player,
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

        if (!isOffhand) {
            int prev = API_MC.player.getInventory().getSelectedSlot();
            INVENTORY_SERVICE.getSlotHandler().attemptSwitch(slot);

            if (simulate)
                API_MC.gameMode.useItemOn(API_MC.player, MAIN_HAND, hit);
            else
                sendSequencedPacket(id -> new ServerboundUseItemOnPacket(MAIN_HAND, hit, id));

            if (swing)
                API_MC.player.swing(MAIN_HAND);
            if (swapBack)
                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(prev);
        } else {
            if (simulate)
                API_MC.gameMode.useItemOn(API_MC.player, OFF_HAND, hit);
            else
                sendSequencedPacket(id -> new ServerboundUseItemOnPacket(OFF_HAND, hit, id));

            if (swing)
                API_MC.player.swing(OFF_HAND);
        }

        //CHAT_SERVICE.sendRaw("interactBlockAt: success");
        return true;
    }

    public static Direction getBlockInteractDir(BlockPos blockPos) {
        Vec3 playerPos = API_MC.player.getEyePosition();
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
            final BlockState state = API_MC.level.getBlockState(blockPos.relative(direction));
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
            API_MC.getConnection().send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));

            API_MC.gameMode.useItemOn(API_MC.player, InteractionHand.OFF_HAND, target);
            if (swing)
                API_MC.player.swing(InteractionHand.MAIN_HAND, false);

            API_MC.getConnection().send(new ServerboundSwingPacket(InteractionHand.OFF_HAND));
            API_MC.getConnection().send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
        } else {
            API_MC.gameMode.useItemOn(API_MC.player, InteractionHand.MAIN_HAND, target);
            if (swing)
                API_MC.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    public static boolean breakBlock(BlockPos pos, double range, boolean rotate, boolean swing, boolean grim, boolean strictDirection, String rotationId) {
        if (API_MC.player == null || API_MC.gameMode == null)
            return false;

        //CHAT_SERVICE.sendRaw(((ClientPlayerInteractionSERVICEAccessor) MC.interactionSERVICE).getBlockBreakingCooldown()+"");

        if (isBlockAirOrFluid(pos)) {
            if (currentBreakingBlock != null && currentBreakingBlock.equals(pos)) {
                currentBreakingBlock = null;
            }
            return false;
        }

        Vec3 eyePos = API_MC.player.getEyePosition();
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
                    (float) getYawToVec(API_MC.player, center),
                    (float) getPitchToVec(API_MC.player, center)
            ));

            if (!ROTATION_SERVICE.getRequestHandler().isCompleted(rotationId)) {
                return false;
            }
        }

        boolean success = API_MC.gameMode.continueDestroyBlock(pos, direction);
        if (swing)
            API_MC.player.swing(MAIN_HAND);

        if (grim && ((DuckMultiPlayerGameMode) API_MC.gameMode).getDestroyDelay() != 0) // https://github.com/GrimAnticheat/Grim/blob/def21633e2bfa52e2dd4afdf91aec3c0ec6d14e7/common/src/main/java/ac/grim/grimac/checks/impl/breaking/FastBreak.java#L28
            return false;

        if (isBlockAirOrFluid(pos)) {  // somehow it happens https://github.com/GrimAnticheat/Grim/blob/def21633e2bfa52e2dd4afdf91aec3c0ec6d14e7/common/src/main/java/ac/grim/grimac/checks/impl/breaking/AirLiquidBreak.java#L18
            currentBreakingBlock = null;
            return false;
        }

        if (currentBreakingBlock == null || !currentBreakingBlock.equals(pos)) {
            currentBreakingBlock = pos;
            API_MC.gameMode.startDestroyBlock(pos, direction);
        } else {
            if (!success) {
                currentBreakingBlock = null;
                return false;
            }
        }

        return true;
    }

    private static boolean isBlockAirOrFluid(BlockPos pos) {
        if (API_MC.level.getBlockState(pos).isAir()) {
            return true;
        }
        FluidState fluidState = API_MC.level.getFluidState(pos);
        return !fluidState.isEmpty();
    }

    public static boolean isPlaceable(BlockPos pos) {
        return isPlaceable(pos, 10);
    }

    public static boolean isPlaceable(BlockPos pos, int distance) {
        AABB blockBox = new AABB(pos);
        for (Entity entity : API_MC.level.entitiesForRendering()) {
            if (entity.distanceToSqr(API_MC.player) > distance) continue;
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
        return API_MC.level.getBlockState(pos).canBeReplaced();
    }

    public static boolean isBed(Block block) {
        return block instanceof BedBlock;
    }

    public static int findHotbarItem(Predicate<ItemStack> predicate) {
        if (API_MC.player == null)
            return -1;

/*        if (predicate.test(MC.player.getInventory().getSelectedItem()))
            return MC.player.getInventory().getSelectedSlot();*/

        ItemStack selected = API_MC.player.getInventory().getSelectedItem();
        if (!selected.isEmpty() && predicate.test(selected))
            return API_MC.player.getInventory().getSelectedSlot();

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = API_MC.player.getInventory().getItem(slot);
            if (stack.isEmpty())
                continue;

            if (predicate.test(stack))
                return slot;
        }

        return -1;
    }

    public static Vec3 getLookVectorFromYawPitch(float yaw, float pitch) {
        float f = (float) Math.cos(-yaw * 0.017453292F - Math.PI);
        float g = (float) Math.sin(-yaw * 0.017453292F - Math.PI);
        float h = - (float) Math.cos(-pitch * 0.017453292F);
        float i = (float) Math.sin(-pitch * 0.017453292F);
        return new Vec3(g * h, i, f * h);
    }
}
