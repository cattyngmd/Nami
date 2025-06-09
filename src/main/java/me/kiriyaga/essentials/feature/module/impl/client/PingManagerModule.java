package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;

public class PingManagerModule extends Module {
    public final IntSetting smoothingStrength = addSetting(new IntSetting("Smoothing Strength", 10, 1, 50));
    public final IntSetting unstableConnectionTimeout = addSetting(new IntSetting("Unstable Connection Timeout", 3, 1, 60));
    public final IntSetting keepAliveInterval = addSetting(new IntSetting("KeepAlive Interval", 900, 250, 2500));
    public final BoolSetting debug = addSetting(new BoolSetting("Debug", false));

    public PingManagerModule() {
        super("Ping Manager", "Allows you to config ping manager settings.", Category.CLIENT, "ping", "manager", "managr", "png", "зштп");
    }
}
