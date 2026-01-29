package me.kiriyaga.nami.api.font;

import me.kiriyaga.nami.impl.feature.impl.client.FontFeature;

import static me.kiriyaga.nami.Nami.FEATURE_SERVICE;

public class FontMetrics {

    private final FontRendererProvider rendererProvider;

    public FontMetrics(FontRendererProvider rendererProvider) {
        this.rendererProvider = rendererProvider;
    }

    public int getHeight() {
        FontFeature fontFeature = FEATURE_SERVICE.getStorage().getByClass(FontFeature.class);
        if (!fontFeature.isEnabled())
            return rendererProvider.getRenderer().lineHeight;
        return Math.round(fontFeature.glyphSize.get() * 0.85f);
    }
}
