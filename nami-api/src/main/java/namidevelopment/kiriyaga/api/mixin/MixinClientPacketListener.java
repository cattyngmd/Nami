package namidevelopment.kiriyaga.api.mixin;

import namidevelopment.kiriyaga.api.contract.FeatureContractService;
import namidevelopment.kiriyaga.api.contract.feature.LatencyFeatureConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static namidevelopment.kiriyaga.api.NamiApi.MC;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Shadow
    private PingDebugMonitor pingDebugMonitor;

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        LatencyFeatureConfig c = FeatureContractService.get(LatencyFeatureConfig.class);
        if (c == null || c.getMode() != LatencyFeatureConfig.Mode.NEW)
            return;

        if (!MC.getDebugOverlay().showNetworkCharts()) {
            this.pingDebugMonitor.tick();
        }
    }
}