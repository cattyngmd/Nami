package namidevelopment.kiriyaga.nami.impl.feature.impl.movement;

import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;

@RegisterFeature
public class HighJumpFeature extends Feature {

    public final DoubleSetting height = addSetting(new DoubleSetting("Height", 0.42, 0.00, 1.0));

    public HighJumpFeature() {
        super("HighJump", "Modifies jump strength.", FeatureCategory.of("Movement"), "highjump");
    }
}
