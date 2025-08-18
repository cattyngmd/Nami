package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;

@RegisterModule
public class BreakManagerModule extends Module {

    public final DoubleSetting maxDistance = addSetting(new DoubleSetting("distance", 5, 0, 6));
    public final EnumSetting<BreakPriority> breakPriority = addSetting(new EnumSetting<>("priority", BreakPriority.CLOSEST));

    public BreakManagerModule() {
        super("break manager", "Allows you to configure break manager.", ModuleCategory.of("client"), "breakmanager", "break");
        if (!this.isEnabled())
            this.toggle();
    }

    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }

    public enum BreakPriority {
        CLOSEST, FIRST, LAST
    }
}
