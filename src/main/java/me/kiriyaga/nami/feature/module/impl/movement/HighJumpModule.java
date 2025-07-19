package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.manager.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.DoubleSetting;

@RegisterModule(category = "movement")
public class HighJumpModule extends Module {

    public final DoubleSetting height = addSetting(new DoubleSetting("height", 0.42, 0.00, 1.0));

    public HighJumpModule() {
        super("high jump", "Modifies jump strength.", ModuleCategory.of("movement"), "highjump", "ршпрогьз");
    }
}
