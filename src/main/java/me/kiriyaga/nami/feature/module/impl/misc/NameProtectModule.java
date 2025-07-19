package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;

@RegisterModule(category = "misc")
public class NameProtectModule extends Module {

    public NameProtectModule() {
        super("name protect", "Changes client name on all client side accessible sides.", ModuleCategory.of("misc"), "nameprotect");
    }
}
