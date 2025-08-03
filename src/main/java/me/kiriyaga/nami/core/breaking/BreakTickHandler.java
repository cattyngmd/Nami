package me.kiriyaga.nami.core.breaking;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.impl.client.BreakManagerModule;
import me.kiriyaga.nami.core.breaking.model.BreakTarget;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

import static me.kiriyaga.nami.Nami.*;

public class BreakTickHandler {

    private final BreakStateHandler stateHandler;
    private final BreakRequestHandler requestHandler;
    private BlockPos currentBreakingBlock = null;

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

        BreakManagerModule module = MODULE_MANAGER.getStorage().getByClass(BreakManagerModule.class);
        requestHandler.removeExpiredTargets(7000);

        BreakTarget target = getNextTarget(module);
        if (target == null)
            return;

        BlockPos pos = target.getPos();

        if (isBlockAirOrFluid(pos)) {
            requestHandler.removeBlock(pos);
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

    private BreakTarget getNextTarget(BreakManagerModule module) {
        return switch (module.breakPriority.get()) {
            case closest -> requestHandler.getTargets().stream()
                    .min(Comparator.comparingDouble(t -> t.getPos().getSquaredDistance(MC.player.getEyePos())))
                    .orElse(null);
            case first -> requestHandler.getTargets().isEmpty() ? null : requestHandler.getTargets().get(0);
            case last -> requestHandler.getTargets().isEmpty() ? null : requestHandler.getTargets().get(requestHandler.getTargets().size() - 1);
        };
    }

    private boolean tryBreak(BlockPos pos) {
        if (MC.interactionManager == null || MC.player == null)
            return false;

        Direction direction = Direction.UP;

        if (currentBreakingBlock == null || !currentBreakingBlock.equals(pos)) {
            MC.interactionManager.attackBlock(pos, direction);
            currentBreakingBlock = pos;
        } else {
            boolean success = MC.interactionManager.updateBlockBreakingProgress(pos, direction);
            if (!success) {
                MC.interactionManager.attackBlock(pos, direction);
            }
        }

        stateHandler.confirmBreaking();
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