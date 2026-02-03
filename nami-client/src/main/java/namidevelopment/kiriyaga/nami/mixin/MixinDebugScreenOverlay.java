package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.api.contract.feature.LatencyFeatureConfig;
import namidevelopment.kiriyaga.nami.impl.feature.client.LatencyFeature;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static namidevelopment.kiriyaga.api.NamiApi.FEATURE_SERVICE;

@Mixin(DebugScreenOverlay.class)
public class MixinDebugScreenOverlay {

    @Inject(method = "showNetworkCharts", at = @At("HEAD"), cancellable = true)
    private void shouldShowPacketSizeAndPingCharts(CallbackInfoReturnable<Boolean> cir) {
        var config = FEATURE_SERVICE.getStorage().getByClass(LatencyFeature.class);
        if (config != null && config.mode.get() == LatencyFeatureConfig.Mode.NEW) {
            cir.setReturnValue(true);
        }
    }
}
