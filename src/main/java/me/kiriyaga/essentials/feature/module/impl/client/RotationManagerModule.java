package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;

public class RotationManagerModule extends Module {
    public final DoubleSetting rotationSpeed = addSetting(new DoubleSetting("Rotation Speed", 5, 1, 30));
    public final DoubleSetting rotationEaseFactor = addSetting(new DoubleSetting("Rotation Ease Factor", 0.15, 0.05, 0.75));
    public final DoubleSetting rotationThreshold = addSetting(new DoubleSetting("Rotation Threshold", 0.25, 0.05, 0.75));

    public RotationManagerModule() {
        super("Rotation Manager", "Allows you to config rotation manager settings.", Category.CLIENT, "rotate", "rotationmanager", "roate", "toationmanager", "кщефеу");
    }
}
