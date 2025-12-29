package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.RenderScreenEvent;
import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("HEAD"), cancellable = true)
    public void noBackground(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage() == null) return;

        NoRenderModule m = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);

        if (m != null && m.isEnabled() && m.noBackground.get() && MC.level != null) {
            ci.cancel();
        }
    }

    @Inject(method = "renderTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void renderInGameBackground(GuiGraphics drawContext, CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage() == null) return;

        NoRenderModule m = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);

        if (m != null && m.isEnabled() && m.noBackground.get() && MC.level != null) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EVENT_MANAGER.post(new RenderScreenEvent(context, null, mouseX, mouseY));
    }
}

