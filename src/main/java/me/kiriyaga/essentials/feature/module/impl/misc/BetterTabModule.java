package me.kiriyaga.essentials.feature.module.impl.misc;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;

public class BetterTabModule extends Module {

    public final IntSetting limit = addSetting(new IntSetting("limit", 300, 25, 15000));
    public final BoolSetting friendsOnly = addSetting(new BoolSetting("friends only", false));
    public final BoolSetting highlighFriends = addSetting(new BoolSetting("highlight friends", true));

    public BetterTabModule() {
        super("better tab", "Extends tab limits and tweaks.", Category.misc, "bettertab");
    }
}
