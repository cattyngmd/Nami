package me.kiriyaga.essentials.feature.module.impl.misc;


import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;

public class AntiPacketKickModule extends Module {

    public AntiPacketKickModule() {
        super("anti packet kick", "Prevents from kicking because of netty exceptions.", Category.misc);
    }
}
