package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.nami.impl.feature.visuals.NoRenderFeature;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import static namidevelopment.kiriyaga.nami.Nami.FEATURE_SERVICE;

@Mixin(BossHealthOverlay.class)
public abstract class MixinBossHealthOverlay {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo info) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && noRender.isEnabled() && noRender.noBossBar.get()) {
            info.cancel();
        }
    }
}