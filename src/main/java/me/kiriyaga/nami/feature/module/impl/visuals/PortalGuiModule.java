package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class PortalGuiModule extends Module {

    public PortalGuiModule() {
        super("portal gui", "Prevents portals from closing your guis.", ModuleCategory.of("visuals"), "portalgui");
    }

}
