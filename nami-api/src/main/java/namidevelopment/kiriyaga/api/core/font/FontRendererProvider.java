package namidevelopment.kiriyaga.api.core.font;

import namidevelopment.kiriyaga.api.client.FontFeature;
import namidevelopment.kiriyaga.api.mixin.DuckFont;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FontDescription;

import static namidevelopment.kiriyaga.api.NamiApi.*;


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
            return API_MC.font;
        }

        fontLoader.init();

        if (cachedRenderer != null
                && cachedSize == fontLoader.getCurrentSize()
                && cachedOversample == fontLoader.getCurrentOversample()) {
            return cachedRenderer;
        }

        EffectGlyph rectangle = ((DuckFont) API_MC.font).getProvider().effect();

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
