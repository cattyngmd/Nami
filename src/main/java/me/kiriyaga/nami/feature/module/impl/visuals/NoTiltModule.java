package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class NoTiltModule extends Module {

    public NoTiltModule() {
        super("no tilt", "Disables damage tilting.", ModuleCategory.of("visuals"), "notilt");
    }
}
