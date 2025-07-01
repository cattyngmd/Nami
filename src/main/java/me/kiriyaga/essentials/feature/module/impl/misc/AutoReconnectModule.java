package me.kiriyaga.essentials.feature.module.impl.misc;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;

public class AutoReconnectModule extends Module {

    public final BoolSetting hardHide = addSetting(new BoolSetting("hard hide", false));
    public final IntSetting delay = addSetting(new IntSetting("delay", 5, 0, 25));

    public AutoReconnectModule() {
        super("auto reconnect", "Automatically reconnects to the specified server.", Category.misc, "autoreconnect");
    }
}
