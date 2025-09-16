package me.kiriyaga.nami.core.breaking;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.impl.client.BreakModule;
import me.kiriyaga.nami.core.breaking.model.BreakTarget;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.RotationUtils.*;

public class BreakTickHandler {

    private final BreakStateHandler stateHandler;
    private final BreakRequestHandler requestHandler;
    private BlockPos currentBreakingBlock = null;
    private long lastAttackBlockTime = 0;

    public BreakTickHandler(BreakStateHandler stateHandler, BreakRequestHandler requestHandler) {
        this.stateHandler = stateHandler;
        this.requestHandler = requestHandler;
    }

    public void init() {
        EVENT_MANAGER.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreTick(PreTickEvent event) {
        stateHandler.resetThisTick();

        if (MC.player == null || MC.interactionManager == null || !requestHandler.hasTarget())
            return;

        BreakModule module = MODULE_MANAGER.getStorage().getByClass(BreakModule.class);
        requestHandler.removeExpiredTargets(7000);

        BreakTarget target = getNextTarget(module);
        if (target == null)
            return;

        BlockPos pos = target.getPos();

        if (isBlockAirOrFluid(pos)) {
            requestHandler.removeBlock(pos);
            if (currentBreakingBlock != null && currentBreakingBlock.equals(pos)) {
                currentBreakingBlock = null;
            }
            return;
        }

        double distSq = MC.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos));
        double maxDistSq = module.maxDistance.get() * module.maxDistance.get();
        if (distSq > maxDistSq)
            return;

        tryBreak(pos);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPostTick(PostTickEvent event) {
        if (stateHandler.shouldCancelBreak()) {
            stateHandler.cancelBreaking();
            if (MC.interactionManager != null)
                MC.interactionManager.cancelBlockBreaking();
        }
    }

    public boolean isBreaking() {
        return stateHandler.isBreaking();
    }

    private BreakTarget getNextTarget(BreakModule module) {
        return switch (module.breakPriority.get()) {
            case CLOSEST -> requestHandler.getTargets().stream()
                    .min(Comparator.comparingDouble(t -> t.getPos().getSquaredDistance(MC.player.getEyePos())))
                    .orElse(null);
            case FIRST -> requestHandler.getTargets().isEmpty() ? null : requestHandler.getTargets().get(0);
            case LAST -> requestHandler.getTargets().isEmpty() ? null : requestHandler.getTargets().get(requestHandler.getTargets().size() - 1);
        };
    }

    private boolean tryBreak(BlockPos pos) {
        if (MC.interactionManager == null || MC.player == null)
            return false;

        Direction direction = Direction.UP;
        BreakModule module = MODULE_MANAGER.getStorage().getByClass(BreakModule.class);

        if (module.rotate.get()) {
            ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(
                    BreakTickHandler.class.getName(),
                    3,
                    (float) getYawToVec(MC.player, Vec3d.ofCenter(pos)),
                    (float) getPitchToVec(MC.player, Vec3d.ofCenter(pos))
            ));

            if (!ROTATION_MANAGER.getRequestHandler().isCompleted(BreakTickHandler.class.getName())) {
                return false;
            }
        }

        long now = System.currentTimeMillis();

        if (currentBreakingBlock == null || !currentBreakingBlock.equals(pos)) {
            boolean instant = MC.world.getBlockState(pos).calcBlockBreakingDelta(MC.player, MC.world, pos) >= 1.0f;

            if (instant) {
                currentBreakingBlock = null;
                MC.interactionManager.attackBlock(pos, direction);
                if (module.swing.get())
                    MC.player.swingHand(Hand.MAIN_HAND);
                stateHandler.confirmBreaking();
                return true;
            } else {
                long attackCooldown = module.grim.get() ? 250 : 0; // grim speedmine checks also include block break delay, even tho we include check for it, breaking is still too fast without grim speedmine disabler
                if (now - lastAttackBlockTime >= attackCooldown) {
                    currentBreakingBlock = pos;
                    MC.interactionManager.attackBlock(pos, direction);
                    lastAttackBlockTime = now;
                }
            }
        } else {
            boolean success = MC.interactionManager.updateBlockBreakingProgress(pos, direction);
            if (!success) {
                currentBreakingBlock = null;
                return false;
            }
        }

        stateHandler.confirmBreaking();
        if (module.swing.get())
            MC.player.swingHand(Hand.MAIN_HAND);
        return true;
    }

    private boolean isBlockAirOrFluid(BlockPos pos) {
        if (MC.world.getBlockState(pos).isAir()) {
            return true;
        }
        FluidState fluidState = MC.world.getFluidState(pos);
        return !fluidState.isEmpty();
    }
}