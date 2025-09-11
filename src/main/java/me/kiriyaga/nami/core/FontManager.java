package me.kiriyaga.nami.core;

import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TrueTypeFontLoader;
import net.minecraft.client.font.FontFilterType;
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

    private static final String FONT_NAME = "impact";

    public void init() {
        TrueTypeFontLoader loader = new TrueTypeFontLoader(
                Identifier.of("nami", FONT_NAME + ".ttf"),
                MODULE_MANAGER.getStorage().getByClass(FontModule.class).glyphSize.get(),
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
        } catch (IOException e) {
            e.printStackTrace();
            customRenderer = MC.textRenderer;
        }
    }

    private static TextRenderer getCustomRenderer() {
        return customRenderer != null ? customRenderer : MC.textRenderer;
    }

    public void drawText(DrawContext context, Text text, int x, int y, boolean shadow) {
        TextRenderer renderer = MODULE_MANAGER.getStorage().getByClass(FontModule.class).isEnabled() ? getCustomRenderer() : MC.textRenderer;
        context.drawText(renderer, text, x, y, 0xFFFFFFFF, shadow);
    }
}
