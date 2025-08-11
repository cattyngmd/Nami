package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.client.HudModule;
import me.kiriyaga.nami.util.ChatAnimationHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {

    protected MixinChatScreen(Text title) {
        super(title);
    }

    @Override
    public void removed() {
        HudModule hud = MODULE_MANAGER.getStorage().getByClass(HudModule.class);
        if (hud == null || !hud.isEnabled() || !hud.chatAnimation.get()) {
            ChatAnimationHelper.setAnimationOffset(0f);
        }
        super.removed();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 0), cancellable = true)
    private void render(DrawContext drawContext, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        HudModule hud = MODULE_MANAGER.getStorage().getByClass(HudModule.class);

        if (hud == null || !hud.isEnabled() || !hud.chatAnimation.get()) {
            ChatAnimationHelper.setAnimationOffset(0f);
            drawContext.fill(2, this.height - 14, this.width - 2, this.height - 2, this.client.options.getTextBackgroundColor(Integer.MIN_VALUE));
            ci.cancel();
        }
    }
}