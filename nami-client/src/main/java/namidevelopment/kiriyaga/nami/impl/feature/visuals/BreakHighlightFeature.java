package namidevelopment.kiriyaga.nami.impl.feature.visuals;

import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;

@RegisterFeature
public class BreakHighlightFeature extends Feature {

    public final BoolSetting fill = addSetting(new BoolSetting("Fill", true));

    public BreakHighlightFeature() {
        super("BreakHighlight", "Highlights block being broken.", FeatureCategory.of("Render"));
    }
}
