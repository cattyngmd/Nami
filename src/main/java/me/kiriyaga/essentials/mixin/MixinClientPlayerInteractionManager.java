package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.event.impl.StartBreakingBlockEvent;
import me.kiriyaga.essentials.feature.module.impl.world.AntiInteractModule;
import me.kiriyaga.essentials.feature.module.impl.world.NoBreakDelayModule;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.essentials.Essentials.EVENT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager{
    @Shadow
    private int blockBreakingCooldown;


    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> call){
        StartBreakingBlockEvent ev = new StartBreakingBlockEvent(blockPos, direction);
        EVENT_MANAGER.post(ev);

        if (ev.isCancelled())
            call.cancel();
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void disableBreakCooldown(CallbackInfoReturnable<Boolean> cir) {
        if (MODULE_MANAGER.getModule(NoBreakDelayModule.class).isEnabled())
            this.blockBreakingCooldown = 0;
    }

    @Inject(method = "interactBlock", at = @At(value = "HEAD"), cancellable = true)
    private void interactBlockHead(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> info) {
        AntiInteractModule antiInteract = me.kiriyaga.essentials.Essentials.MODULE_MANAGER.getModule(AntiInteractModule.class);
        if (antiInteract != null && antiInteract.isEnabled() && antiInteract.spawnPoint.get()) {
            if (player.getWorld() != null) {
                Block block = player.getWorld().getBlockState(hitResult.getBlockPos()).getBlock();
                String dimension = player.getWorld().getDimension().toString();

                if (player.getWorld().getDimension().bedWorks() && antiInteract.isBed(block)) {
                    info.setReturnValue(ActionResult.FAIL);
                    return;
                }
                if (block == Blocks.RESPAWN_ANCHOR && dimension.contains("nether")) {
                    info.setReturnValue(ActionResult.FAIL);
                    return;
                }
            }
        }
    }
}
