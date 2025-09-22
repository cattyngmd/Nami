package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;

@RegisterModule
public class BreakModule extends Module {

    public final DoubleSetting maxDistance = addSetting(new DoubleSetting("Distance", 5, 0, 7));
    public final EnumSetting<BreakPriority> breakPriority = addSetting(new EnumSetting<>("Priority", BreakPriority.CLOSEST));
    public final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    public final BoolSetting grim = addSetting(new BoolSetting("Grim", false));
    public final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));

    public BreakModule() {
        super("Break", "Allows you to configure break manager.", ModuleCategory.of("Client"), "breakmanager", "break");
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
