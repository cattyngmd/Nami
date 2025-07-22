package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;

@RegisterModule
public class NoBreakDelayModule extends Module {

    public NoBreakDelayModule() {
        super("no break delay", "Removes vanilla break delay which increases break speed.", ModuleCategory.of("world"), "nobreakdelay");
    }
}
