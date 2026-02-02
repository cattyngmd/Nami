package namidevelopment.kiriyaga.nami.impl.feature.world;

import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.WhitelistSetting;

@RegisterFeature
public class FastPlaceFeature extends Feature {

    public final IntSetting delay = addSetting(new IntSetting("Delay", 1, 0, 5));
    public final IntSetting startDelay = addSetting(new IntSetting("StartDelay", 10, 0, 50));
    public final WhitelistSetting whitelist = addSetting(new WhitelistSetting("WhiteList", false, WhitelistSetting.Type.ANY));
    public final WhitelistSetting blacklist = addSetting(new WhitelistSetting("BlackList", false, WhitelistSetting.Type.ANY));

    public FastPlaceFeature() {
        super("FastPlace", "Decreases cooldown between any type of use.", FeatureCategory.of("World"), "fastplace");
    }
}
