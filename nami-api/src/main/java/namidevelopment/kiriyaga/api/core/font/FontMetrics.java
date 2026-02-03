package namidevelopment.kiriyaga.api.core.font;

import namidevelopment.kiriyaga.api.contract.FeatureContractService;
import namidevelopment.kiriyaga.api.contract.feature.FontFeatureConfig;

import static namidevelopment.kiriyaga.api.NamiApi.*;

public class FontMetrics {

    private final FontRendererProvider rendererProvider;

    public FontMetrics(FontRendererProvider rendererProvider) {
        this.rendererProvider = rendererProvider;
    }

    public int getHeight() {
        FontFeatureConfig fontFeature = FeatureContractService.get(FontFeatureConfig.class);
        if (!fontFeature.isEnabled())
            return rendererProvider.getRenderer().lineHeight;
        return Math.round(fontFeature.getGlyphSize() * 0.85f);
    }
}
