package me.kiriyaga.nami.impl.feature.impl.miscellaneous;

import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.IntSetting;

@RegisterFeature
public class UnfocusedFpsFeature extends Feature {

    public final IntSetting limit = addSetting(new IntSetting("Limit", 15, 5, 30));

    public UnfocusedFpsFeature() {
        super("UnfocusedFPS", "Limits your frame generation while unfocused.", FeatureCategory.of("Miscellaneous"), "unfocusedcpu");
    }
}
