package me.kiriyaga.nami.feature.gui.newgui.entry;

import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.CAT_FORMAT;
import static me.kiriyaga.nami.Nami.MC;

public class FriendEntry {
    private final String name;
    private final Text displayText;

    public FriendEntry(String name) {
        this.name = name;
        this.displayText = CAT_FORMAT.format(name + " [" + (isOnline(name) ? "{green}Online" : "{red}Offline") + "{reset}]");
    }

    public String getName() { return name; }
    public Text getDisplayText() { return displayText; }

    private boolean isOnline(String name) {
        if (MC.world == null) return false;
        return MC.world.getPlayers().stream()
                .map(p -> p.getGameProfile().getName())
                .anyMatch(n -> n.equalsIgnoreCase(name));
    }
}
