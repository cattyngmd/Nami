package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;

@RegisterModule
public class RotationManagerModule extends Module {
    public final DoubleSetting rotationSpeed = addSetting(new DoubleSetting("speed", 360, 25, 360));
    public final DoubleSetting rotationEaseFactor = addSetting(new DoubleSetting("ease", 1, 0.4, 1));
    public final DoubleSetting rotationThreshold = addSetting(new DoubleSetting("threshold", 3, 0, 15));
    public final IntSetting ticksBeforeRelease = addSetting(new IntSetting("hold", 5, 00, 30));
    public final DoubleSetting jitterAmount = addSetting(new DoubleSetting("jitter amount", 0, 0, 3));
    public final DoubleSetting jitterSpeed = addSetting(new DoubleSetting("jitter speed", 1, 0.015, 1));
    public final DoubleSetting jitterMaxYaw = addSetting(new DoubleSetting("jitter vertical", 1, 0, 3));
    public final DoubleSetting jitterMaxPitch = addSetting(new DoubleSetting("jitter horizontal", 2, 0, 5));
    public final BoolSetting moveFix = addSetting(new BoolSetting("move fix", true));

    public RotationManagerModule() {
        super("rotation manager", "Allows you to config rotation manager settings.", ModuleCategory.of("client"), "rotate", "rotationmanager", "roate", "toationmanager");
        if (!this.isEnabled())
            this.toggle();
        jitterSpeed.setShowCondition(() -> jitterAmount.get()>0);
        jitterMaxYaw.setShowCondition(() -> jitterAmount.get() > 0);
        jitterMaxPitch.setShowCondition(() -> jitterAmount.get() > 0);
    }
    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }
}
