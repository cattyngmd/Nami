package namidevelopment.kiriyaga.nami.impl.feature.miscellaneous;

import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;

@RegisterFeature
public class NameProtectFeature extends Feature {

    public NameProtectFeature() {
        super("NameProtect", "Changes client name on all client side accessible sides.", FeatureCategory.of("Miscellaneous"), "nameprotect");
    }
}
