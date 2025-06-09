package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.feature.module.impl.render.NoRenderModule;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

@Mixin(BackgroundRenderer.class)
public abstract class MixinBackgroundRenderer {
    @ModifyArgs(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Fog;<init>(FFLnet/minecraft/client/render/FogShape;FFFF)V"))
    private static void modifyFogDistance(Args args, Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta) {
        if (fogType == BackgroundRenderer.FogType.FOG_TERRAIN) {
            NoRenderModule noRender = MODULE_MANAGER.getModule(NoRenderModule.class);
            if (noRender.isEnabled() && noRender.isNoFog()) {
                args.set(0, viewDistance * 4);
                args.set(1, viewDistance * 4.25f);
            }
        }
    }
}