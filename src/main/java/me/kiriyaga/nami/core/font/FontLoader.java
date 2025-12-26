package me.kiriyaga.nami.core.font;

import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import net.minecraft.client.font.*;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class FontLoader {

    private FontStorage storage;
    private int currentSize = -1;
    private int currentOversample = -1;
    private FontType lastFont = null;

    public void init() {
        FontModule fontModule = MODULE_MANAGER.getStorage().getByClass(FontModule.class);
        if (fontModule == null) return;

        int newSize = fontModule.glyphSize.get();
        int newOversample = fontModule.oversample.get();
        FontType selectedFont = fontModule.fontType.get();

        if (storage != null && currentSize == newSize && currentOversample == newOversample
                && selectedFont == lastFont) return;

        lastFont = selectedFont;

        TrueTypeFontLoader loader = new TrueTypeFontLoader(
                Identifier.of("nami", selectedFont.getFileName()),
                newSize,
                newOversample,
                TrueTypeFontLoader.Shift.NONE,
                ""
        );

        try {
            Font font = loader.build().orThrow().load(MC.getResourceManager());
            GlyphBaker glyphBaker = new GlyphBaker(MC.getTextureManager(),
                    Identifier.of("nami", selectedFont.getFileName() + "_storage"));

            storage = new FontStorage(glyphBaker);
            storage.setFonts(List.of(new Font.FontFilterPair(font, FontFilterType.FilterMap.NO_FILTER)),
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

    public FontStorage getStorage() {
        return storage;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public int getCurrentOversample() {
        return currentOversample;
    }
}
