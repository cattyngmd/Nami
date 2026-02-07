package namidevelopment.kiriyaga.api.core.font;

import com.mojang.blaze3d.font.GlyphProvider;
import namidevelopment.kiriyaga.api.contract.FeatureContractService;
import namidevelopment.kiriyaga.api.contract.feature.FontFeatureConfig;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.gui.font.providers.TrueTypeGlyphProviderDefinition;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static namidevelopment.kiriyaga.api.NamiApi.*;

public class FontLoader {

    private FontSet storage;
    private int currentSize = -1;
    private int currentOversample = -1;
    private FontType lastFont = null;

    public void init() {
        FontFeatureConfig fontFeature = FeatureContractService.get(FontFeatureConfig.class);
        if (fontFeature == null) return;

        int newSize = fontFeature.getGlyphSize();
        int newOversample = fontFeature.getOversample();
        FontType selectedFont = fontFeature.getFontType();

        if (storage != null && currentSize == newSize && currentOversample == newOversample
                && selectedFont == lastFont) return;

        lastFont = selectedFont;

        TrueTypeGlyphProviderDefinition loader = new TrueTypeGlyphProviderDefinition(
                Identifier.fromNamespaceAndPath("nami", selectedFont.getFileName()),
                newSize,
                newOversample,
                TrueTypeGlyphProviderDefinition.Shift.NONE,
                ""
        );

        try {
            GlyphProvider font = loader.unpack().orThrow().load(MC.getResourceManager());
            GlyphStitcher glyphBaker = new GlyphStitcher(MC.getTextureManager(),
                    Identifier.fromNamespaceAndPath("nami", selectedFont.getFileName() + "_storage"));


            storage = new FontSet(glyphBaker);
            storage.reload(List.of(new GlyphProvider.Conditional(font, FontOption.Filter.ALWAYS_PASS)),
                    Collections.emptySet());

            currentSize = newSize;
            currentOversample = newOversample;
        } catch (IOException e) {
            e.printStackTrace();
            storage = null;
            currentSize = -1;
            currentOversample = -1;
            lastFont = null;
        }
    }

    public FontSet getStorage() {
        return storage;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public int getCurrentOversample() {
        return currentOversample;
    }
}
