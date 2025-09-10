package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

@RegisterModule
public class UnfocusedFpsModule extends Module {

    public final IntSetting limit = addSetting(new IntSetting("limit", 15, 5, 30));

    public UnfocusedFpsModule() {
        super("unfocused fps", "Limits your frame generate usage while unfocused", ModuleCategory.of("misc"), "unfocusedcpu");
    }
}
