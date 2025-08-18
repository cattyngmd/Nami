package me.kiriyaga.nami.feature.module.impl.misc;


import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;

@RegisterModule
public class NoPacketKick extends Module {

    public NoPacketKick() {
        super("no packet kick", "Prevents from kicking because of netty exceptions.", ModuleCategory.of("misc"), "npacketkick", "antipacketkick");
    }
}
