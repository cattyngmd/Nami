package namidevelopment.kiriyaga.api.contract.feature;

import namidevelopment.kiriyaga.api.core.font.FontType;

public interface FontFeatureConfig {

    FontType getFontType();
    int getGlyphSize();
    int getOversample();
    int getShadowDarken();
    boolean isEnabled();
}
