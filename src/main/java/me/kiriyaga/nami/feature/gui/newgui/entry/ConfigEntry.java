package me.kiriyaga.nami.feature.gui.newgui.entry;

import me.kiriyaga.nami.core.config.model.ConfigMeta;
import me.kiriyaga.nami.feature.gui.newgui.base.BaseEntry;
import net.minecraft.network.chat.Component;

import java.time.format.DateTimeFormatter;

import static me.kiriyaga.nami.Nami.CAT_FORMAT;

public class ConfigEntry extends BaseEntry {

    private final String name;
    private final ConfigMeta meta;

    public ConfigEntry(String name, ConfigMeta meta) {
        this.name = name;
        this.meta = meta;

        if (meta == null) {
            this.displayText = CAT_FORMAT.format("Name: {g}"+name + " {reset}Saved: {g}unknown" + "{reset}Author: {g}unknown" + "{reset}Version: {g}unknown");
        } else {
            this.displayText = CAT_FORMAT.format("Name: {g}"+name + " {reset}Saved: {g}" + meta.mode().name() +" {reset}Author: {g}" + meta.author() + " {reset}Version: {g}" + meta.clientVersion());
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public Component getDisplayText() {
        return this.displayText;
    }

    @Override
    public void refreshEntry() {}
}
