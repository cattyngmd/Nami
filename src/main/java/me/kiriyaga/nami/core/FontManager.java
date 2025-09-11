package me.kiriyaga.nami.core;

import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontFilterType;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TrueTypeFontLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class FontManager {

    private static TextRenderer customRenderer;
    private static int currentSize = -1;

    private static final String FONT_NAME = "impact";

    public void init() {
        int newSize = MODULE_MANAGER.getStorage().getByClass(FontModule.class).glyphSize.get();

        if (customRenderer != null && currentSize == newSize) {
            return;
        }

        TrueTypeFontLoader loader = new TrueTypeFontLoader(
                Identifier.of("nami", FONT_NAME + ".ttf"),
                newSize,
                2.0F,
                TrueTypeFontLoader.Shift.NONE,
                ""
        );

        try {
            Font font = loader.build().orThrow().load(MC.getResourceManager());
            FontStorage storage = new FontStorage(
                    MC.getTextureManager(),
                    Identifier.of("nami", FONT_NAME + "_storage")
            );

            storage.setFonts(
                    List.of(new Font.FontFilterPair(font, FontFilterType.FilterMap.NO_FILTER)),
                    Collections.emptySet()
            );

            customRenderer = new TextRenderer(id -> storage, true);
            currentSize = newSize;
        } catch (IOException e) {
            e.printStackTrace();
            customRenderer = MC.textRenderer;
            currentSize = -1;
        }
    }

    private static TextRenderer getCustomRenderer() {
        return customRenderer != null ? customRenderer : MC.textRenderer;
    }

    public void drawText(DrawContext context, Text text, int x, int y, boolean shadow) {
        FontModule fontModule = MODULE_MANAGER.getStorage().getByClass(FontModule.class);

        if (fontModule.isEnabled()) {
            if (fontModule.glyphSize.get() != currentSize) {
                init();
            }
            context.drawText(getCustomRenderer(), text, x, y, 0xFFFFFFFF, shadow);
        } else {
            context.drawText(MC.textRenderer, text, x, y, 0xFFFFFFFF, shadow);
        }
    }
}
