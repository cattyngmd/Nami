package namidevelopment.kiriyaga.nami.impl.feature.impl.miscellaneous;


import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;

@RegisterFeature
public class NoPacketKick extends Feature {

    public NoPacketKick() {
        super("NoPacketKick", "Prevents from kicking because of netty exceptions.", FeatureCategory.of("Miscellaneous"), "npacketkick", "antipacketkick");
    }
}
