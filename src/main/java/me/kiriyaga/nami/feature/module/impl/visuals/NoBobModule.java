package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixininterface.ISimpleOption;
import me.kiriyaga.nami.setting.impl.IntSetting;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class NoBobModule extends Module {

    public NoBobModule() {
        super("no bob", "Disables bobbing.", ModuleCategory.of("visuals"), "nobob");
    }

    /*
    Yeah people dumb as fuck and they REALLY cant find theese in settings
    so i decided to make these cringe modules, just because i dont wanna see any video/screenshot/anyhting with nami
    and tilt/bobbing on it
     */

    @Override
    public void onEnable() {
        if (MC != null && MC.options != null) {
            MC.options.getBobView().setValue(false);
        }
    }
}
