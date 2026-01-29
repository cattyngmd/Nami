package namidevelopment.kiriyaga.nami.impl.feature.impl.miscellaneous;

import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;

@RegisterFeature
public class NameProtectFeature extends Feature {

    public NameProtectFeature() {
        super("NameProtect", "Changes client name on all client side accessible sides.", FeatureCategory.of("Miscellaneous"), "nameprotect");
    }
}
