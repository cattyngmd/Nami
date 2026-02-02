package namidevelopment.kiriyaga.nami.impl.gui.newgui.entry;

import namidevelopment.kiriyaga.nami.impl.gui.newgui.base.BaseEntry;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.Collection;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;
public class FriendEntry extends BaseEntry {
    private final String name;
    private boolean online;

    public FriendEntry(String name) {
        this.name = name;
        refreshEntry();
    }

    public String getName() { return name; }

    @Override
    public Component getDisplayText() { return displayText; }

    @Override
    public void refreshEntry() {
        boolean nowOnline = false;
        if (MC.getConnection() != null) {
            Collection<PlayerInfo> list = MC.getConnection().getOnlinePlayers();
            nowOnline = list.stream().anyMatch(entry -> entry.getProfile().name().equalsIgnoreCase(name));
        }

        if (nowOnline != online || displayText == null) {
            online = nowOnline;
            displayText = CAT_FORMAT.format(name + " [" + (online ? "{green}Online" : "{red}Offline") + "{reset}]");
        }
    }
}
