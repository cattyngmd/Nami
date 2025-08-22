package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

@RegisterModule
public class ViewModelModule extends Module {

    public final BoolSetting eating = addSetting(new BoolSetting("eating", true));
    public final BoolSetting eatingBob = addSetting(new BoolSetting("eating bob", false));
    public final BoolSetting sway = addSetting(new BoolSetting("sway", false));
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
