package me.kiriyaga.nami.feature.gui.newgui.entry;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.Collection;

import static me.kiriyaga.nami.Nami.CAT_FORMAT;
import static me.kiriyaga.nami.Nami.MC;

public class FriendEntry {
    private final String name;
    private boolean online;
    private Text displayText;

    public FriendEntry(String name) {
        this.name = name;
        refreshEntry();
    }

    public String getName() {
        return name;
    }

    public Text getDisplayText() {
        return displayText;
    }

    public void refreshEntry() {
        boolean nowOnline;

        if (MC.getNetworkHandler() == null)
            return;

        Collection<PlayerListEntry> list = MC.getNetworkHandler().getPlayerList();
        nowOnline = list.stream().anyMatch(entry -> entry.getProfile().getName().equalsIgnoreCase(name));
        if (nowOnline != online || displayText == null) {
            online = nowOnline;
            displayText = CAT_FORMAT.format(name + " [" + (online ? "{green}Online" : "{red}Offline") + "{reset}]");
        }
    }
}
