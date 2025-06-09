package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.util.ChatAnimationHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

    @Unique
    private float animationOffset = 20f;
    @Unique
    private long lastUpdateTime = System.currentTimeMillis();

    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci) {
        animationOffset = 20f;
        lastUpdateTime = System.currentTimeMillis();
        ChatAnimationHelper.setAnimationOffset(animationOffset);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        animationOffset = 20f;
        ChatAnimationHelper.setAnimationOffset(animationOffset);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        long now = System.currentTimeMillis();
        float elapsed = (now - lastUpdateTime) / 1000f;
        lastUpdateTime = now;

        if (animationOffset > 0f) {
            animationOffset -= elapsed * 60f;
            if (animationOffset < 0f) animationOffset = 0f;
        }
        ChatAnimationHelper.setAnimationOffset(animationOffset);
    }

    @Inject(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            shift = At.Shift.BEFORE
    ))
    private void beforeInputRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().push();
        context.getMatrices().translate(0, animationOffset, 0);
    }

    @Inject(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            shift = At.Shift.AFTER
    ))
    private void afterInputRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Redirect(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"
    ))
        private void redirectFill(DrawContext context, int x1, int y1, int x2, int y2, int color) {
            int width = MINECRAFT.getWindow().getScaledWidth();
            int height = MINECRAFT.getWindow().getScaledHeight();

            if (x1 == 2 && x2 == width - 2 && y1 >= height - 14 && y2 <= height - 2) {
                context.fill(x1, (int)(y1 + animationOffset), x2, (int)(y2 + animationOffset), color);
            } else {
                context.fill(x1, y1, x2, y2, color);
            }
        }

}
