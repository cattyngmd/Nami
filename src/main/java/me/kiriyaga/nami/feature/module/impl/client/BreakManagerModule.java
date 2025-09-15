package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

@RegisterModule
public class BreakManagerModule extends Module {

    public final DoubleSetting maxDistance = addSetting(new DoubleSetting("distance", 5, 0, 7));
    public final EnumSetting<BreakPriority> breakPriority = addSetting(new EnumSetting<>("priority", BreakPriority.CLOSEST));
    public final BoolSetting swing = addSetting(new BoolSetting("swing", true));
    public final BoolSetting grim = addSetting(new BoolSetting("grim", false));
    public final BoolSetting rotate = addSetting(new BoolSetting("rotate", true));

    public BreakManagerModule() {
        super("break", "Allows you to configure break manager.", ModuleCategory.of("client"), "breakmanager", "break");
        if (!this.isEnabled())
            this.toggle();
        breakPriority.setShow(false);
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
