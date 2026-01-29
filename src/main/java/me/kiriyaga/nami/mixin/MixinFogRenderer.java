package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.kiriyaga.nami.impl.feature.impl.visuals.NoRenderFeature;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.kiriyaga.nami.Nami.FEATURE_SERVICE;

@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {

    @ModifyExpressionValue(
            method = "getBuffer",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;fogEnabled:Z")
    )
    private boolean modifyFogEnabled(boolean original) {
        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && noRender.isEnabled() && noRender.noFog.get()) {
            return false;
        }
        return original;
    }
}
