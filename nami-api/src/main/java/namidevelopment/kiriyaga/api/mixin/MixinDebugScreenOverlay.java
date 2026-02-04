package namidevelopment.kiriyaga.api.mixin;

import namidevelopment.kiriyaga.api.contract.FeatureContractService;
import namidevelopment.kiriyaga.api.contract.feature.LatencyFeatureConfig;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(DebugScreenOverlay.class)
public class MixinDebugScreenOverlay {

    @Inject(method = "showNetworkCharts", at = @At("HEAD"), cancellable = true)
    private void shouldShowPacketSizeAndPingCharts(CallbackInfoReturnable<Boolean> cir) {
        LatencyFeatureConfig config = FeatureContractService.get(LatencyFeatureConfig.class);
        if (config != null && config.getMode() == LatencyFeatureConfig.Mode.NEW) {
            cir.setReturnValue(true);
        }
    }
}
