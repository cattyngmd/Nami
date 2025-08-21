package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;

@RegisterModule
public class NoEntityTraceModule extends Module {

    public final BoolSetting playerOnly = addSetting(new BoolSetting("player only", false));
    public final BoolSetting pickaxeOnly = addSetting(new BoolSetting("pickaxe only", false));

    public NoEntityTraceModule() {
        super("no entity trace", "Prevents entity from blocking your client raycast.", ModuleCategory.of("combat"), "noentitytrace");
    }
}
