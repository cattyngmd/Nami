package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;

@RegisterModule
public class BetterTabModule extends Module {

    public final IntSetting limit = addSetting(new IntSetting("limit", 300, 25, 2500));
    public final BoolSetting friendsOnly = addSetting(new BoolSetting("friends only", false));
    public final BoolSetting highlighFriends = addSetting(new BoolSetting("highlight friends", true));

    public BetterTabModule() {
        super("better tab", "Extends tab limits and tweaks.", ModuleCategory.of("misc"), "bettertab");
    }
}
