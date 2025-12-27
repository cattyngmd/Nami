package me.kiriyaga.nami.mixin;

import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.systems.RenderSystem;
import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import me.kiriyaga.nami.Nami;
import net.minecraft.client.font.GlyphAtlasTexture;
import net.minecraft.client.gl.GpuSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalDouble;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(GlyphAtlasTexture.class)
public abstract class MixinGlyphAtlasTexture {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInitPost(CallbackInfo ci) {
        FontModule fontModule = MODULE_MANAGER.getStorage().getByClass(FontModule.class);

        if (fontModule != null && fontModule.isEnabled()) {
            GpuSampler sampler = RenderSystem.getDevice().createSampler(
                    AddressMode.REPEAT,
                    AddressMode.REPEAT,
                    fontModule.filterMode.get(),
                    fontModule.filterMode.get(),
                    fontModule.anisotropy.get(),
                    OptionalDouble.of(fontModule.maxLOD.get())
            );

            ((AbstractTextureAccessor) this).setSampler(sampler);
        }
    }
}


