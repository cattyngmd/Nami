package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.feature.module.impl.render.NoRenderModule;
import me.kiriyaga.essentials.manager.ModuleManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), cancellable = true)
    public void noBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        NoRenderModule m = MODULE_MANAGER.getModule(NoRenderModule.class);

        if (m.isEnabled() && m.isNoBackground())
            ci.cancel();
    }
}

