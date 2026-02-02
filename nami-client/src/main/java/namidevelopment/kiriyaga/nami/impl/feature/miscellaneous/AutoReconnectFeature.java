package namidevelopment.kiriyaga.nami.impl.feature.miscellaneous;

import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.api.model.setting.IntSetting;

@RegisterFeature
public class AutoReconnectFeature extends Feature {

    public final BoolSetting hardHide = addSetting(new BoolSetting("HideMenu", false));
    public final IntSetting delay = addSetting(new IntSetting("Delay", 5, 0, 80));

    public AutoReconnectFeature() {
        super("AutoReconnect", "Automatically reconnects to the specified server.", FeatureCategory.of("Miscellaneous"), "autoreconnect");
    }
}
