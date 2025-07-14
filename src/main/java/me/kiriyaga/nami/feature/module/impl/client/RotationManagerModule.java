package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;

public class RotationManagerModule extends Module {
    public final DoubleSetting rotationSpeed = addSetting(new DoubleSetting("speed", 360, 25, 360));
    public final DoubleSetting rotationEaseFactor = addSetting(new DoubleSetting("ease", 1, 0.4, 1));
    public final DoubleSetting rotationThreshold = addSetting(new DoubleSetting("threshold", 3, 0, 15));
    public final IntSetting ticksBeforeRelease = addSetting(new IntSetting("hold", 5, 00, 30));
    public final BoolSetting moveFix = addSetting(new BoolSetting("move fix", true));
    public final DoubleSetting jitterAmount = addSetting(new DoubleSetting("jitter amount", 0, 0, 15));
    public final DoubleSetting jitterSpeed = addSetting(new DoubleSetting("jitter speed", 0.3, 0.015, 1));

    public RotationManagerModule() {
        super("rotation manager", "Allows you to config rotation manager settings.", Category.client, "rotate", "rotationmanager", "roate", "toationmanager", "кщефеу");
        if (!this.isEnabled())
            this.toggle();
    }
    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }
}
