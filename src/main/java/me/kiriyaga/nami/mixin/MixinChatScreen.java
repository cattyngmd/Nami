package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.client.HudModule;
import me.kiriyaga.nami.util.ChatAnimationHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {

    protected MixinChatScreen(Component title) {
        super(title);
    }


    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private void onFillBackground(Args args) {
        HudModule hud = MODULE_MANAGER.getStorage().getByClass(HudModule.class);

        if (hud != null && hud.isEnabled() && hud.chatAnimation.get()) {
            args.set(4, 0);
        }
    }
}