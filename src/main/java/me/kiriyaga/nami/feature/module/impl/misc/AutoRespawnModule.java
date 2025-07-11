package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.BoolSetting;

public class AutoRespawnModule extends Module {

    public final BoolSetting sendCords = addSetting(new BoolSetting("send cords", true));

    public AutoRespawnModule() {
        super("auto respawn", "Automatically respawns after death.", Category.misc, "autorespawn");
    }
}
