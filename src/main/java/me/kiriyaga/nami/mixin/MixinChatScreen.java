package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.client.HUDModule;
import me.kiriyaga.nami.util.ChatAnimationHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {

    private float animatedHeight = 0f;
    private long lastTime = System.currentTimeMillis();
    private static final int MAX_HEIGHT = 14;

    protected MixinChatScreen(Text title) {
        super(title);
    }

    @Override
    public void removed() {
        animatedHeight = 0;
        ChatAnimationHelper.setAnimationOffset(animatedHeight);
        super.removed();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
    private void redirectFill(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        HUDModule hud = MODULE_MANAGER.getStorage().getByClass(HUDModule.class);

        if (hud == null)
            return;

        if (!hud.chatAnimation.get() || !hud.isEnabled()){
            animatedHeight = 14;
            ChatAnimationHelper.setAnimationOffset(animatedHeight);
            context.fill(2, this.height - 14, this.width - 2, this.height - 2, this.client.options.getTextBackgroundColor(Integer.MIN_VALUE)); // net.minecraft.client.gui.screen l176
            return;
        }

        long now = System.currentTimeMillis();
        float delta = (now - lastTime) / 1000f;
        lastTime = now;

        animatedHeight = Math.min(animatedHeight + delta * 60f, MAX_HEIGHT);
        ChatAnimationHelper.setAnimationOffset(animatedHeight);
        context.fill(x1, this.height - (int) animatedHeight, x2, y2, color);
    }
}
