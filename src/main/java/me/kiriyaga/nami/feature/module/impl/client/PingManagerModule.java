package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;

@RegisterModule
public class PingManagerModule extends Module {
    public final IntSetting smoothingStrength = addSetting(new IntSetting("smoothing", 10, 1, 50));
    public final IntSetting unstableConnectionTimeout = addSetting(new IntSetting("unstable", 3, 1, 60));
    public final IntSetting keepAliveInterval = addSetting(new IntSetting("interval", 900, 250, 2500));
    public final BoolSetting debug = addSetting(new BoolSetting("debug", false));

    public PingManagerModule() {
        super("ping manager", "Allows you to config ping manager settings.", ModuleCategory.of("client"), "ping", "manager", "managr", "png", "зштп");
        if (!this.isEnabled())
            this.toggle();
    }

    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }
}
