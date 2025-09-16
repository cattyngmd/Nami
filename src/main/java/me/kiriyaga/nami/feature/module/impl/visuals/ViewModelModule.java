package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;

@RegisterModule
public class ViewModelModule extends Module {

    public final BoolSetting hand = addSetting(new BoolSetting("hand", false));
    public final BoolSetting eating = addSetting(new BoolSetting("eating", true));
    public final DoubleSetting eatingBob = addSetting(new DoubleSetting("eating bob", 1.00, 0.00, 1.00));
    public final BoolSetting sway = addSetting(new BoolSetting("sway", true));
    public final DoubleSetting scale = addSetting(new DoubleSetting("scale", 1.0, 0.1, 2));
    public final DoubleSetting posX = addSetting(new DoubleSetting("posX", 0.0, -3, 3));
    public final DoubleSetting posY = addSetting(new DoubleSetting("posY", 0.0, -3, 3));
    public final DoubleSetting posZ = addSetting(new DoubleSetting("posZ", 0.0, -3, 3));
    public final DoubleSetting rotX = addSetting(new DoubleSetting("rotX", 0.0, -180.0, 180.0));
    public final DoubleSetting rotY = addSetting(new DoubleSetting("rotY", 0.0, -180.0, 180.0));
    public final DoubleSetting rotZ = addSetting(new DoubleSetting("rotZ", 0.0, -180.0, 180.0));

    public ViewModelModule() {
        super("viewmodel", "Modifies hand position, scale, and rotation.", ModuleCategory.of("visuals"), "vm", "handpos");
    eatingBob.setShowCondition(() -> eating.get());
    }
}
