package me.kiriyaga.essentials.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.kiriyaga.essentials.event.events.Render2DEvent;
import me.kiriyaga.essentials.Essentials;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.essentials.Essentials.LOGGER;
import static me.kiriyaga.essentials.Essentials.EVENT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(method = "render", at = @At("RETURN"))
    public void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
        EVENT_MANAGER.post(new Render2DEvent(context, tickDelta));
    }
}

