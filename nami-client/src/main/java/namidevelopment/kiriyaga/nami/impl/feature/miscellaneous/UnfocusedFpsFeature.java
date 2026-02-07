package namidevelopment.kiriyaga.nami.impl.feature.miscellaneous;

import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.setting.IntSetting;

@RegisterFeature
public class UnfocusedFpsFeature extends Feature {

    public final IntSetting limit = addSetting(new IntSetting("Limit", 15, 15, 30));

    public UnfocusedFpsFeature() {
        super("UnfocusedFPS", "Limits your frame generation while unfocused.", FeatureCategory.of("Miscellaneous"), "unfocusedcpu");
    }
}
