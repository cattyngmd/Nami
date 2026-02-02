package namidevelopment.kiriyaga.nami.impl.feature.impl.miscellaneous;

import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;

@RegisterFeature
public class AutoReconnectFeature extends Feature {

    public final BoolSetting hardHide = addSetting(new BoolSetting("HideMenu", false));
    public final IntSetting delay = addSetting(new IntSetting("Delay", 5, 0, 80));

    public AutoReconnectFeature() {
        super("AutoReconnect", "Automatically reconnects to the specified server.", FeatureCategory.of("Miscellaneous"), "autoreconnect");
    }
}
