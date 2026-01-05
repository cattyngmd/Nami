package me.kiriyaga.nami.feature.gui.newgui.entry;

import me.kiriyaga.nami.feature.gui.newgui.base.BaseEntry;
import net.minecraft.network.chat.Component;

public class ConfigEntry extends BaseEntry {

    private final String name;

    public ConfigEntry(String name) {
        this.name = name;
        this.displayText = Component.literal(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public Component getDisplayText() {
        return displayText;
    }

    @Override
    public void refreshEntry() {
    }
}
