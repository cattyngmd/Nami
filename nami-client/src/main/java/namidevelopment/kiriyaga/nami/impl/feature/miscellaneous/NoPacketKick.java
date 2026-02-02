package namidevelopment.kiriyaga.nami.impl.feature.miscellaneous;


import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;

@RegisterFeature
public class NoPacketKick extends Feature {

    public NoPacketKick() {
        super("NoPacketKick", "Prevents from kicking because of netty exceptions.", FeatureCategory.of("Miscellaneous"), "npacketkick", "antipacketkick");
    }
}
