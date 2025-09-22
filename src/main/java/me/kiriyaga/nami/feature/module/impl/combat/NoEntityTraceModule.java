package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;

@RegisterModule
public class NoEntityTraceModule extends Module {

    public final BoolSetting playerOnly = addSetting(new BoolSetting("PlayerOnly", false));
    public final BoolSetting pickaxeOnly = addSetting(new BoolSetting("PickaxeOnly", false));

    public NoEntityTraceModule() {
        super("NoEntityTrace", "Prevents entity from blocking your client raycast.", ModuleCategory.of("Combat"), "noentitytrace");
    }
}
