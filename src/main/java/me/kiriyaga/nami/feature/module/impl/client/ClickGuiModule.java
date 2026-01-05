package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class ClickGuiModule extends Module {

    public final DoubleSetting scale = addSetting(new DoubleSetting("Scale", 1.00, 0.50, 1.50));
    public final BoolSetting lines = addSetting(new BoolSetting("Lines", true));
    public final BoolSetting descriptions = addSetting(new BoolSetting("Descriptions", true));
    public final IntSetting guiAlpha = addSetting(new IntSetting("UIAlpha", 70, 0, 255));
    public final BoolSetting fade = addSetting(new BoolSetting("Fade", true));
    public final BoolSetting blur = addSetting(new BoolSetting("Blur", true));
    public final BoolSetting panels = addSetting(new BoolSetting("Panels", false));
    public final BoolSetting moduleFill = addSetting(new BoolSetting("ModuleFill", true));
    public final BoolSetting coloredText = addSetting(new BoolSetting("ColoredText", false));
    public final BoolSetting background = addSetting(new BoolSetting("Background", true));

    public ClickGuiModule() {
        super("ClickGui", "Opens client UI.", ModuleCategory.of("Client"), "clickgui","click", "gui", "menu", "clckgui");
    }

    @Override
    public void onEnable(){
        if (MC == null || MC.mouseHandler == null)
            return;

        NAVIGATE_PANEL.resetActive();
        CLICK_GUI_SCREEN.scale = this.scale.get().floatValue(); // bad
        CLICK_GUI_SCREEN.setPreviousScreen(MC.screen);

        MC.setScreen(CLICK_GUI_SCREEN);
        this.toggle();
    }
}
