package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

import static me.kiriyaga.nami.Nami.CLICK_GUI;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class FontModule extends Module {

    public final IntSetting glyphSize = addSetting(new IntSetting("size", 15, 8, 32));

    public FontModule() {
        super("font", "Custom client font renderer.", ModuleCategory.of("client"), "f", "customfont");
    }
}
