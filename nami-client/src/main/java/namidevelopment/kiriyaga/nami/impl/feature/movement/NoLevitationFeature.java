package namidevelopment.kiriyaga.nami.impl.feature.movement;

import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;

@RegisterFeature
public class NoLevitationFeature extends Feature {

    public final BoolSetting noSlowFall = addSetting(new BoolSetting("NoSlowFall", false));

    public NoLevitationFeature() {
        super("NoLevitation", "Removes levitation status effect.", FeatureCategory.of("Movement"), "antilevitation");
    }
}
