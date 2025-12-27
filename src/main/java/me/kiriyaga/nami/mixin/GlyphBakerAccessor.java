package me.kiriyaga.nami.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.GlyphAtlasTexture;
import net.minecraft.client.font.GlyphBaker;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(GlyphBaker.class)
@Environment(EnvType.CLIENT)
public interface GlyphBakerAccessor {

    @Accessor("glyphAtlases")
    List<GlyphAtlasTexture> getGlyphAtlases();

    @Accessor("textureManager")
    TextureManager getTextureManager();

    @Accessor("fontId")
    Identifier getFontId();
}
