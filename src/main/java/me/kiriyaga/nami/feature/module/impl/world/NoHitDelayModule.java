package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;

public class NoHitDelayModule extends Module {

    public NoHitDelayModule() {
        super("no hit delay", "Removes vanilla hit delay which increases hit speed.", Category.world, "nohitdelay");
    }
}
