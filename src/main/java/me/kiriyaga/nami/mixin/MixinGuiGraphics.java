package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import me.kiriyaga.nami.Nami;
import me.kiriyaga.nami.core.font.FontManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphics {

    @ModifyVariable(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V", at = @At("HEAD"), index = 1, argsOnly = true)
    private Font drawText(Font original) {
        FontModule fontModule = MODULE_MANAGER.getStorage().getByClass(FontModule.class);
        if (fontModule != null && fontModule.isEnabled() && fontModule.global.get())
            return Nami.FONT_MANAGER.rendererProvider.getRenderer();
        return original;
    }
}
