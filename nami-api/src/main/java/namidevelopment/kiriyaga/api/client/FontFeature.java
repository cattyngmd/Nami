package namidevelopment.kiriyaga.api.client;

import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.api.font.FontType;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.setting.EnumSetting;
import namidevelopment.kiriyaga.api.model.setting.IntSetting;

@RegisterFeature
public class FontFeature extends Feature {

    public final EnumSetting<FontType> fontType = addSetting(new EnumSetting<>("Font", FontType.VERDANAPRO));
    public final IntSetting shadowDarken = addSetting(new IntSetting("ShadowDarken", 75, 60, 100));
    public final IntSetting glyphSize = addSetting(new IntSetting("Size", 9, 6, 24));
    public final IntSetting oversample = addSetting(new IntSetting("Oversample", 2, 2, 8));

    public FontFeature() {
        super("Font", "Custom font renderer.", FeatureCategory.of("Client"), "f", "customfont");
    }
}
