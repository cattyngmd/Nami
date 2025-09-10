package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;

@RegisterModule
public class NoBobModule extends Module {

    public NoBobModule() {
        super("no bob", "Disables bobbing.", ModuleCategory.of("visuals"), "nobob");
    }
}
