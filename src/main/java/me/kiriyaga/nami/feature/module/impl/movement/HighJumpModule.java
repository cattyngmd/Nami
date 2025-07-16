package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.manager.RotationManager;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;

import static me.kiriyaga.nami.Nami.MINECRAFT;
import static me.kiriyaga.nami.Nami.ROTATION_MANAGER;

public class HighJumpModule extends Module {

    public final DoubleSetting height = addSetting(new DoubleSetting("height", 0.42, 0.00, 1.0));

    public HighJumpModule() {
        super("high jump", "Modifies jump strength.", Category.movement, "highjump", "ршпрогьз");
    }
}
