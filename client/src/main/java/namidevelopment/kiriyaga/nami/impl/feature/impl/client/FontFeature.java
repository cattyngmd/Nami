package namidevelopment.kiriyaga.nami.impl.feature.impl.client;

import namidevelopment.kiriyaga.nami.api.font.FontType;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.EnumSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;

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
