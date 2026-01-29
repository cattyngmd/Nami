package me.kiriyaga.nami.impl.feature.impl.movement;

import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;

@RegisterFeature
public class NoLevitationFeature extends Feature {

    public final BoolSetting noSlowFall = addSetting(new BoolSetting("NoSlowFall", false));

    public NoLevitationFeature() {
        super("NoLevitation", "Removes levitation status effect.", FeatureCategory.of("Movement"), "antilevitation");
    }
}
