package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.feature.gui.ClickGuiScreen;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;

import static me.kiriyaga.essentials.Essentials.CLICK_GUI;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class ClickGuiModule extends Module {

    public ClickGuiModule() {
        super("ClickGui", "Opens click gui", Category.CLIENT, "click", "gui", "menu", "clckgui", "сдшслпгш");
    }
    @Override
    public void onEnable(){
        if (MINECRAFT == null || MINECRAFT.mouse == null)
            return;

        MINECRAFT.setScreen(CLICK_GUI);
        this.toggle();
    }
}
