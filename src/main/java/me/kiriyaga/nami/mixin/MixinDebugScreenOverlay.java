package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.client.LatencyModule;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(DebugScreenOverlay.class)
public class MixinDebugScreenOverlay {

    @Inject(method = "showNetworkCharts", at = @At("HEAD"), cancellable = true)
    private void shouldShowPacketSizeAndPingCharts(CallbackInfoReturnable<Boolean> cir) {
        var config = MODULE_MANAGER.getStorage().getByClass(LatencyModule.class);
        if (config != null && config.fastLatencyMode.get() == LatencyModule.mode.NEW) {
            cir.setReturnValue(true);
        }
    }
}
