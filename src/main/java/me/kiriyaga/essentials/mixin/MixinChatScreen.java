package me.kiriyaga.essentials.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        long now = System.currentTimeMillis();
        float elapsed = (now - lastUpdateTime) / 1000f;
        lastUpdateTime = now;

        if (animationOffset > 0f) {
            animationOffset -= elapsed * 60f; // speed anim
            if (animationOffset < 0f) animationOffset = 0f;
        }

        context.getMatrices().push();
        context.getMatrices().translate(0, animationOffset, 0);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void afterRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().pop();
    }
}