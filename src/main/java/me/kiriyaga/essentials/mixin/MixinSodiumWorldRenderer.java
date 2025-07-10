package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.feature.module.impl.render.NoRenderModule;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

@Mixin(SodiumWorldRenderer.class)
public class MixinSodiumWorldRenderer {
    @ModifyVariable(method = "setupTerrain", at = @At("HEAD"), argsOnly = true)
    private FogParameters modifyFogParameters(FogParameters fogParameters) {
        if (MODULE_MANAGER.getModule(NoRenderModule.class) == null) return fogParameters;

        if (MODULE_MANAGER.getModule(NoRenderModule.class).isNoFog()) return FogParameters.NONE;

        return fogParameters;
    }
}