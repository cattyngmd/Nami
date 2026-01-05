package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

@RegisterModule
public class LatencyModule extends Module {

    public enum mode {
        OLD,
        NEW,
        OFF
    }

    public final EnumSetting<mode> fastLatencyMode = addSetting(new EnumSetting<>("Mode", mode.NEW));
    public final IntSetting smoothingStrength = addSetting(new IntSetting("Smooth", 10, 1, 50));
    public final IntSetting unstableConnectionTimeout = addSetting(new IntSetting("Unstable", 3, 1, 60));
    public final IntSetting keepAliveInterval = addSetting(new IntSetting("Interval", 900, 250, 2500));

    public LatencyModule() {
        super("Latency", "Defines how ping should be calculated.", ModuleCategory.of("Client"), "ping", "manager", "managr", "png");
        if (!this.isEnabled())
            this.toggle();

        smoothingStrength.setShowCondition(() -> fastLatencyMode.get() == mode.OLD);
        unstableConnectionTimeout.setShowCondition(() -> fastLatencyMode.get() != mode.OFF);
        keepAliveInterval.setShowCondition(() -> fastLatencyMode.get() == mode.OLD);
    }

    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }
}
