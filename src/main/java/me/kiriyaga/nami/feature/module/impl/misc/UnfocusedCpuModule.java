package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.manager.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.IntSetting;

@RegisterModule(category = "misc")
public class UnfocusedCpuModule extends Module {

    public final IntSetting limit = addSetting(new IntSetting("limit", 15, 5, 30));

    public UnfocusedCpuModule() {
        super("unfocused cpu", "Limits your cpu/frame generate usage while unfocused", ModuleCategory.of("misc"), "unfocusedcpu");
    }
}
