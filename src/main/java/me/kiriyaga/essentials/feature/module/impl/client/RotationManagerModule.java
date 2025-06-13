package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;

public class RotationManagerModule extends Module {
    public final DoubleSetting rotationSpeed = addSetting(new DoubleSetting("Rotation Speed", 25, 1, 60));
    public final DoubleSetting rotationEaseFactor = addSetting(new DoubleSetting("Rotation Ease Factor", 0.9, 0.05, 1));
    public final DoubleSetting rotationThreshold = addSetting(new DoubleSetting("Rotation Threshold", 0, 0, 15));
    public final IntSetting ticksBeforeRelease = addSetting(new IntSetting("Rotation Hold", 30, 00, 120));
    public final IntSetting holdTicksLimit = addSetting(new IntSetting("Hold Limit", 15, 0, 120));

    public RotationManagerModule() {
        super("Rotation Manager", "Allows you to config rotation manager settings.", Category.CLIENT, "rotate", "rotationmanager", "roate", "toationmanager", "кщефеу");
    }
}
