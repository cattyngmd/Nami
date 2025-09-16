package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

import static me.kiriyaga.nami.Nami.CLICK_GUI;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class ClickGuiModule extends Module {

    public final DoubleSetting scale = addSetting(new DoubleSetting("scale", 1.00, 0.50, 1.50));
    public final BoolSetting moduleFill = addSetting(new BoolSetting("module fill", true));
    public final BoolSetting lines = addSetting(new BoolSetting("lines", true));
    public final BoolSetting expandedIdentifier = addSetting(new BoolSetting("identifier", false));
    public final BoolSetting descriptions = addSetting(new BoolSetting("descriptions", true));
    public final IntSetting guiAlpha = addSetting(new IntSetting("ui alpha", 50, 0, 255));
    public final BoolSetting fade = addSetting(new BoolSetting("fade", true));
    public final BoolSetting background = addSetting(new BoolSetting("background", true));
    public final IntSetting backgroundAlpha = addSetting(new IntSetting("alpha", 75, 0, 255));

    public ClickGuiModule() {
        super("click gui", "Opens client UI.", ModuleCategory.of("client"), "clickgui","click", "gui", "menu", "clckgui");
        backgroundAlpha.setShowCondition(background::get);
    }

    @Override
    public void onEnable(){
        if (MC == null || MC.mouse == null)
            return;

        CLICK_GUI.scale = this.scale.get().floatValue(); // bad
        CLICK_GUI.setPreviousScreen(MC.currentScreen);

        MC.setScreen(CLICK_GUI);
        this.toggle();
    }
}
