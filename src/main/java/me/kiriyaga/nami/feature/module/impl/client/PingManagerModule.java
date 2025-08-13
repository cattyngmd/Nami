package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;

@RegisterModule
public class PingManagerModule extends Module {

    public enum FastLatencyMode {
        OLD,
        NEW,
        OFF
    }

    public final EnumSetting<FastLatencyMode> fastLatencyMode = addSetting(new EnumSetting<>("fast latency", FastLatencyMode.NEW));
    public final IntSetting smoothingStrength = addSetting(new IntSetting("smoothing", 10, 1, 50));
    public final IntSetting unstableConnectionTimeout = addSetting(new IntSetting("unstable", 3, 1, 60));
    public final IntSetting keepAliveInterval = addSetting(new IntSetting("interval", 900, 250, 2500));

    public PingManagerModule() {
        super("ping manager", "Allows you to config ping manager settings.", ModuleCategory.of("client"), "ping", "manager", "managr", "png", "зштп");

        smoothingStrength.setShowCondition(() -> fastLatencyMode.get() == FastLatencyMode.OLD);
        unstableConnectionTimeout.setShowCondition(() -> fastLatencyMode.get() != FastLatencyMode.OFF);
        keepAliveInterval.setShowCondition(() -> fastLatencyMode.get() == FastLatencyMode.OLD);
    }

    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }
}
