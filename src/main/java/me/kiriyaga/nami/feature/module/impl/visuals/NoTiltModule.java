package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class NoTiltModule extends Module {

    public NoTiltModule() {
        super("no tilt", "Disables damage tilting.", ModuleCategory.of("visuals"), "notilt", "тщешде");
    }
    
    /*
    Yeah people dumb as fuck and they REALLY cant find theese in settings
    so i decided to make these cringe modules, just because i dont wanna see any video/screenshot/anyhting with nami
    and tilt/bobbing on it
     */

    @Override
    public void onEnable() {
        if (MC != null && MC.options != null) {
            MC.options.getDamageTiltStrength().setValue(0.00);
        }
    }
}
