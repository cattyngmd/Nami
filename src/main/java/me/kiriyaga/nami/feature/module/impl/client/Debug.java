package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;

import java.util.Set;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class Debug extends Module {

    public final BoolSetting inventory = addSetting(new BoolSetting("inventory", false));
    public final BoolSetting auraGet = addSetting(new BoolSetting("aura", false));
    public final BoolSetting ping = addSetting(new BoolSetting("ping", false));

    public Debug() {
        super("debug", ".", ModuleCategory.of("client"), "debug");
    }
}
