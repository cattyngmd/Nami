package me.kiriyaga.nami.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(GlyphStitcher.class)
@Environment(EnvType.CLIENT)
public interface DuckGlyphStitcher {

    @Accessor("textures")
    List<FontTexture> getTextures();

    @Accessor("textureManager")
    TextureManager getTextureManager();

    @Accessor("texturePrefix")
    Identifier getTexturePrefix();
}
