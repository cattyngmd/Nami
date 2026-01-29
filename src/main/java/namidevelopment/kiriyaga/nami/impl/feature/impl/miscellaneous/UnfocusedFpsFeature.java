package namidevelopment.kiriyaga.nami.impl.feature.impl.miscellaneous;

import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;

@RegisterFeature
public class UnfocusedFpsFeature extends Feature {

    public final IntSetting limit = addSetting(new IntSetting("Limit", 15, 5, 30));

    public UnfocusedFpsFeature() {
        super("UnfocusedFPS", "Limits your frame generation while unfocused.", FeatureCategory.of("Miscellaneous"), "unfocusedcpu");
    }
}
