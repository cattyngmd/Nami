package me.kiriyaga.essentials.feature.module.impl.world;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;

public class AutoRespawnModule extends Module {

    public final BoolSetting sendCords = addSetting(new BoolSetting("send cords", true));

    public AutoRespawnModule() {
        super("auto respawn", "Automatically respawns after death.", Category.WORLD, "autorespawn");
    }
}
