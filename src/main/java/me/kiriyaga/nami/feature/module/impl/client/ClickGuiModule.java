package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.BoolSetting;

import static me.kiriyaga.nami.Nami.CLICK_GUI;
import static me.kiriyaga.nami.Nami.MINECRAFT;

public class ClickGuiModule extends Module {

    public final BoolSetting background = addSetting(new BoolSetting("background", false));
    public final BoolSetting descriptions = addSetting(new BoolSetting("descriptions", true));

    public ClickGuiModule() {
        super("click gui", "Opens client UI.", Category.client, "clickgui","click", "gui", "menu", "clckgui", "сдшслпгш");
    }

    @Override
    public void onEnable(){
        if (MINECRAFT == null || MINECRAFT.mouse == null)
            return;

        MINECRAFT.setScreen(CLICK_GUI);
        this.toggle();
    }
}
