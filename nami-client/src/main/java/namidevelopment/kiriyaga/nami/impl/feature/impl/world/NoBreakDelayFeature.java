package namidevelopment.kiriyaga.nami.impl.feature.impl.world;

import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;

@RegisterFeature
public class NoBreakDelayFeature extends Feature {

    public NoBreakDelayFeature() {
        super("NoBreakDelay", "Removes vanilla break delay which increases break speed.", FeatureCategory.of("World"), "nobreakdelay");
    }
}
