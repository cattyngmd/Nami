package me.kiriyaga.nami.core.font;

import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import me.kiriyaga.nami.mixin.TextRendererAccessor;
import net.minecraft.client.font.EffectGlyph;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.GlyphProvider;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.util.Identifier;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class FontRendererProvider {

    private final FontLoader fontLoader;
    private TextRenderer cachedRenderer;
    private int cachedSize = -1;
    private int cachedOversample = -1;

    public FontRendererProvider(FontLoader fontLoader) {
        this.fontLoader = fontLoader;
    }

    public TextRenderer getRenderer() {
        FontModule fontModule = MODULE_MANAGER.getStorage().getByClass(FontModule.class);

        if (!fontModule.isEnabled()) {
            return MC.textRenderer;
        }

        if (cachedRenderer != null && cachedSize == fontModule.glyphSize.get() && cachedOversample == fontModule.oversample.get()) {
            return cachedRenderer;
        }

        EffectGlyph rectangle = ((TextRendererAccessor) MC.textRenderer).getFonts().getRectangleGlyph();

        cachedRenderer = new TextRenderer(new TextRenderer.GlyphsProvider() {
            @Override
            public GlyphProvider getGlyphs(StyleSpriteSource font) {
                return fontLoader.getStorage().getGlyphs(true);
            }

            @Override
            public EffectGlyph getRectangleGlyph() {
                return rectangle;
            }
        });

        cachedSize = fontModule.glyphSize.get();
        cachedOversample = fontModule.oversample.get();

        return cachedRenderer;
    }
}
