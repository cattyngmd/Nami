package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.misc.UnfocusedCpuModule;
import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(InactivityFpsLimiter.class)
public class MixinInactivityFpsLimiter {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void updateHead(CallbackInfoReturnable<Integer> info) {
        if (MODULE_MANAGER.getStorage().getByClass(UnfocusedCpuModule.class).isEnabled() && !MC.isWindowFocused()) {
            info.setReturnValue(MODULE_MANAGER.getStorage().getByClass(UnfocusedCpuModule.class).limit.get());
        }
    }
}