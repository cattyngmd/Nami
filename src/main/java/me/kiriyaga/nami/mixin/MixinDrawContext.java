package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import me.kiriyaga.nami.Nami;
import me.kiriyaga.nami.core.font.FontManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(DrawContext.class)
public abstract class MixinDrawContext {

    @ModifyVariable(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)V", at = @At("HEAD"), index = 1, argsOnly = true)
    private TextRenderer drawText(TextRenderer original) {
        FontModule fontModule = MODULE_MANAGER.getStorage().getByClass(FontModule.class);
        if (fontModule != null && fontModule.isEnabled() && fontModule.global.get())
            return Nami.FONT_MANAGER.rendererProvider.getRenderer();
        return original;
    }
}
