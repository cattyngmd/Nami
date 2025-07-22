package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;

@RegisterModule
public class NoEntityTrace extends Module {

    public NoEntityTrace() {
        super("no entity trace", "Prevents entity from blocking your client raycast.", ModuleCategory.of("combat"), "noentitytrace", "тщутешенекфсу");
    }
}
