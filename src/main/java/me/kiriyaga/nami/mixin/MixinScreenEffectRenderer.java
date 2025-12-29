package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(ScreenEffectRenderer.class)
public class MixinScreenEffectRenderer {

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void onRenderFireOverlay(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, TextureAtlasSprite sprite, CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage() == null) return;

        NoRenderModule noRender = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);
        if (noRender != null && noRender.isEnabled() && noRender.noFire.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderWater", at = @At("HEAD"), cancellable = true)
    private static void onRenderUnderwaterOverlay(Minecraft client, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage() == null) return;

        NoRenderModule noRender = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);
        if (noRender != null && noRender.isEnabled() && noRender.noLiguid.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderTex", at = @At("HEAD"), cancellable = true)
    private static void render(TextureAtlasSprite sprite, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage() == null) return;

        NoRenderModule noRender = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);
        if (noRender != null && noRender.isEnabled() && noRender.noWall.get()) {
            ci.cancel();
        }
    }
}
