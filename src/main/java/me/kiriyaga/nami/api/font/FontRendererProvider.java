package me.kiriyaga.nami.api.font;

import me.kiriyaga.nami.impl.feature.impl.client.FontFeature;
import me.kiriyaga.nami.mixin.DuckFont;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FontDescription;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.FEATURE_SERVICE;

public class FontRendererProvider {

    private final FontLoader fontLoader;
    private Font cachedRenderer;
    private int cachedSize = -1;
    private int cachedOversample = -1;

    public FontRendererProvider(FontLoader fontLoader) {
        this.fontLoader = fontLoader;
    }

    public Font getRenderer() {
        FontFeature fontFeature = FEATURE_SERVICE.getStorage().getByClass(FontFeature.class);

        if (!fontFeature.isEnabled()) {
            return MC.font;
        }

        fontLoader.init();

        if (cachedRenderer != null
                && cachedSize == fontLoader.getCurrentSize()
                && cachedOversample == fontLoader.getCurrentOversample()) {
            return cachedRenderer;
        }

        EffectGlyph rectangle = ((DuckFont) MC.font).getProvider().effect();

        cachedRenderer = new Font(new Font.Provider() {
            @Override
            public GlyphSource glyphs(FontDescription font) {
                return fontLoader.getStorage().source(true);
            }

            @Override
            public EffectGlyph effect() {
                return rectangle;
            }
        });

        cachedSize = fontLoader.getCurrentSize();
        cachedOversample = fontLoader.getCurrentOversample();

        return cachedRenderer;
    }
}
