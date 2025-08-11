package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.gui.screen.HudEditorScreen;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class HudEditorModule extends Module {
    public final BoolSetting background = addSetting(new BoolSetting("background", false));
    public final IntSetting backgroundAlpha = addSetting(new IntSetting("background alpha", 200, 0, 255));

    public HudEditorModule() {
        super("hud editor", "Opens HUD editor screen.", ModuleCategory.of("client"), "hudeditor", "hudedit", "he", "худ", "худе");
        backgroundAlpha.setShowCondition(background::get);
    }

    @Override
    public void onEnable() {
        if (MC == null || MC.mouse == null) return;

        MC.setScreen(new HudEditorScreen());
        this.toggle();
    }
}
