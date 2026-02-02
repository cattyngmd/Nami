package namidevelopment.kiriyaga.nami.impl.feature.movement;

import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;

@RegisterFeature
public class NoLevitationFeature extends Feature {

    public final BoolSetting noSlowFall = addSetting(new BoolSetting("NoSlowFall", false));

    public NoLevitationFeature() {
        super("NoLevitation", "Removes levitation status effect.", FeatureCategory.of("Movement"), "antilevitation");
    }
}
