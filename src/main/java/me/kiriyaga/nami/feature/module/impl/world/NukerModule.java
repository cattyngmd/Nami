package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.StartBreakingBlockEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.WhitelistSetting;
import me.kiriyaga.nami.util.InteractionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.util.RotationUtils.getClosestPointToEye;

@RegisterModule
public class NukerModule extends Module {

    public enum NukerMode { SPHERE, SELECTIVE}
    public final EnumSetting<NukerMode> mode = addSetting(new EnumSetting<>("Mode", NukerMode.SPHERE));
    public final DoubleSetting range = addSetting(new DoubleSetting("Range", 3.0, 1.0, 6.0));
    public final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    public final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    public final BoolSetting grim = addSetting(new BoolSetting("Grim", false));
    public final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));
    public final BoolSetting safeOnly = addSetting(new BoolSetting("SafeOnly", false));
    public final BoolSetting flatten = addSetting(new BoolSetting("Flatten", true));
    public final WhitelistSetting whitelist = addSetting(new WhitelistSetting("Whitelist", true, WhitelistSetting.Type.BLOCK));

    private Set<BlockPos> selectiveTargets = new HashSet<>();
    private Block blockBeingMined = null;

    public NukerModule() {
        super("Nuker", "Automatically breaks blocks around you.", ModuleCategory.of("World"));
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPreTickEvent(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;

        BlockPos targetBlock = null;

        if (mode.get() == NukerMode.SPHERE) {
            targetBlock = looking();
            if (targetBlock == null) {
                targetBlock = closest();
            }
        } else if (mode.get() == NukerMode.SELECTIVE) {
            selectiveTargets.removeIf(pos -> !canBreak(pos));
            if (!selectiveTargets.isEmpty()) {
                targetBlock = closestSelective();
            }
        }

        if (targetBlock != null)
            if (!InteractionUtils.breakBlock(targetBlock, range.get(), rotate.get(), swing.get(), grim.get(), strictDirection.get(), this.name));
               // selectiveTargets.remove(targetBlock);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockStartBreak(StartBreakingBlockEvent event) {
        if (mode.get() != NukerMode.SELECTIVE) return;

        BlockState state = MC.level.getBlockState(event.blockPos);
        if (state.getBlock().defaultDestroyTime() == -1.0f || state.isAir()) return;

        if (selectiveTargets.isEmpty()) {
            blockBeingMined = state.getBlock();
            int radius = 5;
            BlockPos origin = event.blockPos;

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = origin.offset(x, y, z);
                        BlockState checkState = MC.level.getBlockState(pos);
                        if (checkState.getBlock() == blockBeingMined && canBreak(pos)) {
                            selectiveTargets.add(pos);
                        }
                    }
                }
            }
        }
    }

    private BlockPos looking() {
        HitResult hit = MC.player.pick(range.get(), 1.0f, false);
        if (hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            if (canBreak(pos)) return pos;
        }
        return null;
    }

    private BlockPos closest() {
        Vec3 eyePos = MC.player.getEyePosition(1.0f);
        int radius = 6;
        BlockPos playerPos = MC.player.blockPosition();
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    if (!canBreak(pos)) continue;

                    double dist = eyePos.distanceToSqr(Vec3.atCenterOf(pos));
                    if (dist < closestDistance) {
                        closest = pos;
                        closestDistance = dist;
                    }
                }
            }
        }
        return closest;
    }

    private BlockPos closestSelective() {
        if (selectiveTargets.isEmpty()) return null;

        Vec3 eyePos = MC.player.getEyePosition(1.0f);
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;

        Iterator<BlockPos> it = selectiveTargets.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next();
            if (!canBreak(pos)) {
                it.remove();
                continue;
            }
            double dist = eyePos.distanceToSqr(Vec3.atCenterOf(pos));
            if (dist < closestDistance) {
                closest = pos;
                closestDistance = dist;
            }
        }
        return closest;
    }

    private boolean canBreak(BlockPos pos) {
        if (MC.level == null) return false;
        BlockState state = MC.level.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.BEDROCK || state.isAir()) return false;
        if (flatten.get() && pos.getY() <= MC.player.getBlockY() - 1) return false;
        if (whitelist.get() && !whitelist.isWhitelisted(BuiltInRegistries.BLOCK.getKey(block))) return false;

        if (safeOnly.get()) {
            BlockState above = MC.level.getBlockState(pos.above());
            if (above.getBlock() == Blocks.SAND || above.getBlock() == Blocks.GRAVEL) return false;

            BlockPos[] adjacent = new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west(), pos.above()};
            for (BlockPos adj : adjacent) {
                BlockState adjState = MC.level.getBlockState(adj);
                if (adjState.getBlock() == Blocks.LAVA || adjState.getBlock() == Blocks.WATER) return false;
            }
        }

        Vec3 eyePos = MC.player.getEyePosition();
        AABB blockBox = new AABB(pos);
        Vec3 lookDir = getClosestPointToEye(eyePos, blockBox).subtract(eyePos).normalize();
        Vec3 reachEnd = eyePos.add(lookDir.scale(range.get()));
        if (blockBox.clip(eyePos, reachEnd).isEmpty())
            return false;

        return true;
    }
}
