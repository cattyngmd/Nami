package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.feature.module.impl.misc.UnfocusedCpuModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;
import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

@Mixin(InactivityFpsLimiter.class)
public class MixinInactivityFpsLimiter {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void updateHead(CallbackInfoReturnable<Integer> info) {
        if (MODULE_MANAGER.getModule(UnfocusedCpuModule.class).isEnabled() && !MINECRAFT.isWindowFocused()) {
            info.setReturnValue(MODULE_MANAGER.getModule(UnfocusedCpuModule.class).limit.get());
        }
    }
}