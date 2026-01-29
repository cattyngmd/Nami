package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.impl.feature.impl.miscellaneous.UnfocusedFpsFeature;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.FEATURE_SERVICE;

@Mixin(FramerateLimitTracker.class)
public class MixinFramerateLimitTracker {
    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    private void updateHead(CallbackInfoReturnable<Integer> info) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        UnfocusedFpsFeature Feature = FEATURE_SERVICE.getStorage().getByClass(UnfocusedFpsFeature.class);
        if (Feature == null) return;

        if (Feature.isEnabled() && (MC == null || !MC.isWindowActive())) {
            info.setReturnValue(Feature.limit.get());
        }
    }
}