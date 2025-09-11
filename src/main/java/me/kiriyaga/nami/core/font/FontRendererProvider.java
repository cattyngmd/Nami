package me.kiriyaga.nami.core.font;

import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import net.minecraft.client.font.TextRenderer;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class FontRendererProvider {

    private final FontLoader fontLoader;
    private TextRenderer customRenderer;

    public FontRendererProvider(FontLoader fontLoader) {
        this.fontLoader = fontLoader;
    }

    public TextRenderer getRenderer() {
        FontModule fontModule = MODULE_MANAGER.getStorage().getByClass(FontModule.class);

        if (!fontModule.isEnabled()) {
            return MC.textRenderer;
        }

        if (fontLoader.getCurrentSize() != fontModule.glyphSize.get() ||
                fontLoader.getCurrentOversample() != fontModule.oversample.get()) {
            fontLoader.init();
            customRenderer = new TextRenderer(id -> fontLoader.getStorage(), true);
        }

        return customRenderer != null ? customRenderer : MC.textRenderer;
    }
}
