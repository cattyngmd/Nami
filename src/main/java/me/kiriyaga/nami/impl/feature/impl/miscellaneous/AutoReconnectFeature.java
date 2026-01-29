package me.kiriyaga.nami.impl.feature.impl.miscellaneous;

import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.IntSetting;

@RegisterFeature
public class AutoReconnectFeature extends Feature {

    public final BoolSetting hardHide = addSetting(new BoolSetting("HideMenu", false));
    public final IntSetting delay = addSetting(new IntSetting("Delay", 5, 0, 80));

    public AutoReconnectFeature() {
        super("AutoReconnect", "Automatically reconnects to the specified server.", FeatureCategory.of("Miscellaneous"), "autoreconnect");
    }
}
