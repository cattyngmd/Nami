package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.manager.module.RegisterModule;

@RegisterModule(category = "world")
public class NoHitDelayModule extends Module {

    public NoHitDelayModule() {
        super("no hit delay", "Removes vanilla hit delay which increases hit speed.", ModuleCategory.of("world"), "nohitdelay");
    }
}
