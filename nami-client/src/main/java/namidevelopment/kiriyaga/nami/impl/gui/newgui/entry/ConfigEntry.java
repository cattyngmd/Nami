package namidevelopment.kiriyaga.nami.impl.gui.newgui.entry;

import namidevelopment.kiriyaga.api.core.config.model.ConfigMeta;
import namidevelopment.kiriyaga.nami.impl.gui.newgui.base.BaseEntry;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.api.NamiApi.CAT_FORMAT;

public class ConfigEntry extends BaseEntry {

    private final String name;
    private final ConfigMeta meta;

    public ConfigEntry(String name, ConfigMeta meta) {
        this.name = name;
        this.meta = meta;

        if (meta == null) {
            this.displayText = CAT_FORMAT.format("Name: {global}"+name + " {white}Saved: {global}unknown" + "{white}Author: {global}unknown" + "{white}Version: {global}unknown");
        } else {
            this.displayText = CAT_FORMAT.format("Name: {global}"+name + " {white}Saved: {global}" + meta.mode().name() +" {white}Author: {global}" + meta.author() + " {white}Version: {global}" + meta.clientVersion());
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
