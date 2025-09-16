package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;

@RegisterModule
public class OldAnimationsModule extends Module {

    public OldAnimationsModule() {
        super("old animations", "Makes your hands animated like 1.8.", ModuleCategory.of("visuals"), "oldanimations");
    }
}