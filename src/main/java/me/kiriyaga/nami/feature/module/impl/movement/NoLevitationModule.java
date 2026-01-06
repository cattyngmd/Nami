package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;

@RegisterModule
public class NoLevitationModule extends Module {

    public final BoolSetting noSlowFall = addSetting(new BoolSetting("NoSlowFall", false));

    public NoLevitationModule() {
        super("NoLevitation", "Removes levitation status effect.", ModuleCategory.of("Movement"), "antilevitation");
    }
}
