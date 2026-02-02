package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.nami.event.impl.Render2DEvent;
import namidevelopment.kiriyaga.nami.impl.feature.visuals.NoRenderFeature;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static namidevelopment.kiriyaga.nami.Nami.EVENT_SERVICE;
import static namidevelopment.kiriyaga.nami.Nami.FEATURE_SERVICE;

@Mixin(Gui.class)
public class MixinGui {

    @Inject(method = "render", at = @At("RETURN"))
    public void onRender(GuiGraphics context, DeltaTracker renderTickCounter, CallbackInfo ci) {
        EVENT_SERVICE.post(new Render2DEvent(context, renderTickCounter));
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void onRenderStatusEffectOverlay(CallbackInfo info) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && noRender.isEnabled() && noRender.noPotIcon.get()) {
            info.cancel();
        }
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderPortalOverlay(GuiGraphics context, float nauseaStrength, CallbackInfo ci) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && noRender.isEnabled() && noRender.noPortal.get()) {
            ci.cancel();
        }
    }

    @ModifyArgs(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTextureOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/Identifier;F)V", ordinal = 0))
    private void onRenderPumpkinOverlay(org.spongepowered.asm.mixin.injection.invoke.arg.Args args) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && noRender.isEnabled() && noRender.noPumpkin.get()) {
            args.set(2, 0f);
        }
    }

    @ModifyArgs(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTextureOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/Identifier;F)V", ordinal = 1))
    private void onRenderPowderedSnowOverlay(org.spongepowered.asm.mixin.injection.invoke.arg.Args args) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && noRender.isEnabled() && noRender.noPowderedSnow.get()) {
            args.set(2, 0f);
        }
    }

    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    private void onRenderVignetteOverlay(GuiGraphics context, Entity entity, CallbackInfo ci) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && noRender.isEnabled() && noRender.noVignette.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderConfusionOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderNausea(GuiGraphics context, float distortionStrength, CallbackInfo ci) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && noRender.isEnabled() && noRender.noNausea.get()) {
            ci.cancel();
        }
    }
}

