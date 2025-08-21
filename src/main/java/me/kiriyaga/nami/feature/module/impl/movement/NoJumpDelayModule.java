package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;

@RegisterModule
public class NoJumpDelayModule extends Module {

    public NoJumpDelayModule() {
        super("no jump delay", "Removes vanilla jump delay which increases movement speed.", ModuleCategory.of("movement"), "nojumpdelay");
    }
}
