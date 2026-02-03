package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.api.client.HudFeature;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static namidevelopment.kiriyaga.api.NamiApi.FEATURE_SERVICE;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {

    protected MixinChatScreen(Component title) {
        super(title);
    }


    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private void onFillBackground(Args args) {
        HudFeature hud = FEATURE_SERVICE.getStorage().getByClass(HudFeature.class);

        if (hud != null && hud.isEnabled() && hud.chatAnimation.get()) {
            args.set(4, 0);
        }
    }
}