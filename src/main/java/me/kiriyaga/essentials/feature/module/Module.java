package me.kiriyaga.essentials.feature.module;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.EVENT_MANAGER;

public abstract class Module {

    protected final String name;
    protected final String description;
    protected final String[] aliases;

    private boolean enabled = false;
    private String displayInfo = "";

    public Module(String name, String description, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean state) {
        if (this.enabled == state) return;

        this.enabled = state;

        if (enabled) {
            EVENT_MANAGER.register(this);
            CHAT_MANAGER.sendTransient(name + " toggled §aon");
            onEnable();
        } else {
            EVENT_MANAGER.unregister(this);
            CHAT_MANAGER.sendTransient(name + " toggled §coff");
            onDisable();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAliases() {
        return aliases;
    }

    public boolean matches(String input) {
        String lower = input.toLowerCase();
        if (lower.equals(name.toLowerCase())) return true;
        for (String alias : aliases) {
            if (lower.equals(alias.toLowerCase())) return true;
        }
        return false;
    }

    public String getDisplayName() {
        if (displayInfo != null && !displayInfo.isEmpty()) {
            return name + "[" + displayInfo + "]";
        }
        return name;
    }

    public void setDisplayInfo(String info) {
        this.displayInfo = info;
    }

    public String getDisplayInfo() {
        return displayInfo;
    }

    protected void onEnable() {}

    protected void onDisable() {}
}
