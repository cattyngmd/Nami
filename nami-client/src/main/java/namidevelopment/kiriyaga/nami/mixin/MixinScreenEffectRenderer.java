package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.nami.impl.feature.visuals.NoRenderFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class MixinScreenEffectRenderer {

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void onRenderFireOverlay(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, TextureAtlasSprite sprite, CallbackInfo ci) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && noRender.isEnabled() && noRender.noFire.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderWater", at = @At("HEAD"), cancellable = true)
    private static void onRenderUnderwaterOverlay(Minecraft client, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && noRender.isEnabled() && noRender.noLiguid.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderTex", at = @At("HEAD"), cancellable = true)
    private static void render(TextureAtlasSprite sprite, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && noRender.isEnabled() && noRender.noWall.get()) {
            ci.cancel();
        }
    }
}
