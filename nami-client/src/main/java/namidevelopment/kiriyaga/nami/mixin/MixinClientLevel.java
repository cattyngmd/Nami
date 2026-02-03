package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.nami.impl.feature.visuals.NoRenderFeature;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static namidevelopment.kiriyaga.api.NamiApi.FEATURE_SERVICE;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel {
    @Inject(method = "addDestroyBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("HEAD"), cancellable = true)
    private void addDestroyBlockEffect(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        NoRenderFeature nr = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (nr != null && nr.isEnabled() && nr.noBlockBreak.get())
            ci.cancel();
    }
    @Inject(method = "addBreakingBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V", at = @At("HEAD"), cancellable = true)
    private void addBreakingBlockEffect(BlockPos blockPos, Direction direction, CallbackInfo ci) {
        NoRenderFeature nr = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (nr != null && nr.isEnabled() && nr.noBlockBreak.get())
            ci.cancel();
    }
}