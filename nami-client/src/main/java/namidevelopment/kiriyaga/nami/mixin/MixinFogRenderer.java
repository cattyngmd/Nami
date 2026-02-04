package namidevelopment.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import namidevelopment.kiriyaga.nami.impl.feature.visuals.NoRenderFeature;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static namidevelopment.kiriyaga.api.NamiApi.FEATURE_SERVICE;

@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {

    @ModifyExpressionValue(method = "getBuffer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;fogEnabled:Z"))
    private boolean getBuffer(boolean original) {
        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && noRender.isEnabled() && noRender.noFog.get()) {
            return false;
        }
        return original;
    }
}
