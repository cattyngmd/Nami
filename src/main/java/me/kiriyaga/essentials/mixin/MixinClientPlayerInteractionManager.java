package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.event.impl.StartBreakingBlockEvent;
import me.kiriyaga.essentials.feature.module.impl.world.NoBreakDelayModule;
import net.minecraft.client.network.ClientPlayerInteractionManager;
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
}
