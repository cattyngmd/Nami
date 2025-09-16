package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;

@RegisterModule
public class ReachModule extends Module {

    public final DoubleSetting block = addSetting(new DoubleSetting("block", 1.00,0.00, 3.00));
    public final DoubleSetting entity = addSetting(new DoubleSetting("entity", 0.00,0.00, 3.00));

    public ReachModule() {
        super("reach", "Extends player reach values.", ModuleCategory.of("combat"));
    }
}
