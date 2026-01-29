package me.kiriyaga.nami.impl.feature.impl.miscellaneous;


import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.RegisterFeature;

@RegisterFeature
public class NoPacketKick extends Feature {

    public NoPacketKick() {
        super("NoPacketKick", "Prevents from kicking because of netty exceptions.", FeatureCategory.of("Miscellaneous"), "npacketkick", "antipacketkick");
    }
}
