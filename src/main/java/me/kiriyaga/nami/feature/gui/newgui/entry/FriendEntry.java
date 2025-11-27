package me.kiriyaga.nami.feature.gui.newgui.entry;

import me.kiriyaga.nami.feature.gui.newgui.base.BaseEntry;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.Collection;

import static me.kiriyaga.nami.Nami.*;

public class FriendEntry extends BaseEntry {
    private final String name;
    private boolean online;

    public FriendEntry(String name) {
        this.name = name;
        refreshEntry();
    }

    public String getName() { return name; }

    @Override
    public Text getDisplayText() { return displayText; }

    @Override
    public void refreshEntry() {
        boolean nowOnline = false;
        if (MC.getNetworkHandler() != null) {
            Collection<PlayerListEntry> list = MC.getNetworkHandler().getPlayerList();
            nowOnline = list.stream().anyMatch(entry -> entry.getProfile().getName().equalsIgnoreCase(name));
        }

        if (nowOnline != online || displayText == null) {
            online = nowOnline;
            displayText = CAT_FORMAT.format(name + " [" + (online ? "{green}Online" : "{red}Offline") + "{reset}]");
        }
    }
}
