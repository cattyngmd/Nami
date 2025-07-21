package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;

import static me.kiriyaga.nami.Nami.CLICK_GUI;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule(category = "client")
public class ClickGuiModule extends Module {

    public final DoubleSetting scale = addSetting(new DoubleSetting("scale", 1.00, 0.50, 1.50));
    public final BoolSetting moduleFill = addSetting(new BoolSetting("module fill", true));
    public final BoolSetting background = addSetting(new BoolSetting("background", true));
    public final BoolSetting descriptions = addSetting(new BoolSetting("descriptions", true));

    public ClickGuiModule() {
        super("click gui", "Opens client UI.", ModuleCategory.of("client"), "clickgui","click", "gui", "menu", "clckgui", "сдшслпгш");
    }

    @Override
    public void onEnable(){
        if (MC == null || MC.mouse == null)
            return;

        CLICK_GUI.scale = this.scale.get().floatValue(); // bad

        MC.setScreen(CLICK_GUI);
        this.toggle();
    }
}
