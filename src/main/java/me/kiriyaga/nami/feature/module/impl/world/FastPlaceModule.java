package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.IntSetting;

public class FastPlaceModule extends Module {

    public final IntSetting delay = addSetting(new IntSetting("delay", 1, 0, 5));
    public final IntSetting startDelay = addSetting(new IntSetting("start delay", 10, 0, 50));

    public FastPlaceModule() {
        super("fast place", "Decreases cooldown between any type of use.", Category.world, "fastplace");
    }
}
