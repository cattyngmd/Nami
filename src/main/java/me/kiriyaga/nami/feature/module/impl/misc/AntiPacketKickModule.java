package me.kiriyaga.nami.feature.module.impl.misc;


import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;

@RegisterModule(category = "misc")
public class AntiPacketKickModule extends Module {

    public AntiPacketKickModule() {
        super("anti packet kick", "Prevents from kicking because of netty exceptions.", ModuleCategory.of("misc"));
    }
}
