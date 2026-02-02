package namidevelopment.kiriyaga.nami.impl.feature.visuals;

import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;

@RegisterFeature
public class BreakHighlightFeature extends Feature {

    public final BoolSetting fill = addSetting(new BoolSetting("Fill", true));

    public BreakHighlightFeature() {
        super("BreakHighlight", "Highlights block being broken.", FeatureCategory.of("Render"));
    }
}
