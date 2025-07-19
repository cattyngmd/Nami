package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;

@RegisterModule(category = "misc")
public class AutoRespawnModule extends Module {

    public final BoolSetting sendCords = addSetting(new BoolSetting("send cords", true));

    public AutoRespawnModule() {
        super("auto respawn", "Automatically respawns after death.", ModuleCategory.of("misc"), "autorespawn");
    }
}
