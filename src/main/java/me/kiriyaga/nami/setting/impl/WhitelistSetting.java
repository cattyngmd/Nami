package me.kiriyaga.nami.setting.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.kiriyaga.nami.feature.command.Command;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.COMMAND_MANAGER;

public class WhitelistSetting extends BoolSetting {
    private final Set<Identifier> whitelist = new HashSet<>();
    private final String moduleName;

    public WhitelistSetting(String name, boolean defaultValue, String moduleName) {
        super(name, defaultValue);
        this.moduleName = moduleName.toLowerCase();

        COMMAND_MANAGER.registerCommand(new WhitelistCommand());
    }

    public Set<Identifier> getWhitelist() {
        return whitelist;
    }

    public boolean isWhitelisted(Identifier id) {
        return whitelist.contains(id);
    }

    public boolean addToWhitelist(String idStr) {
        Identifier id = Identifier.tryParse("minecraft:" + idStr);
        if (id == null) return false;
        return whitelist.add(id);
    }

    public boolean removeFromWhitelist(String idStr) {
        Identifier id = Identifier.tryParse("minecraft:" + idStr);
        if (id == null) return false;
        return whitelist.remove(id);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject()) return;
        JsonObject obj = json.getAsJsonObject();

        if (obj.has("enabled") && obj.get("enabled").isJsonPrimitive()) {
            this.value = obj.get("enabled").getAsBoolean();
        }

        if (obj.has("items") && obj.get("items").isJsonArray()) {
            whitelist.clear();
            for (JsonElement element : obj.getAsJsonArray("items")) {
                if (element.isJsonPrimitive()) {
                    Identifier id = Identifier.tryParse(element.getAsString());
                    if (id != null) whitelist.add(id);
                }
            }
        }
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", value);
        JsonArray items = new JsonArray();
        for (Identifier id : whitelist) {
            items.add(id.toString());
        }
        obj.add("items", items);
        return obj;
    }

    private class WhitelistCommand extends Command {
        public WhitelistCommand() {
            super(moduleName, "Manage whitelist for " + moduleName + " whitelist setting.");
        }

        @Override
        public void execute(String[] args) {
            if (args.length < 3) {
                CHAT_MANAGER.sendPersistent(moduleName, "Usage: §7." + moduleName + " whitelist <add/del> <item>");
                return;
            }

            if (!args[0].equalsIgnoreCase("whitelist")) {
                CHAT_MANAGER.sendPersistent(moduleName, "Usage: §7." + moduleName + " whitelist <add/del> <item>");
                return;
            }

            String action = args[1];
            String itemName = args[2].toLowerCase().replace("minecraft:", "");
            boolean result = false;

            switch (action.toLowerCase()) {
                case "add":
                    result = addToWhitelist(itemName);
                    if (result) {
                        CHAT_MANAGER.sendPersistent(moduleName, "Added: §7minecraft:" + itemName);
                    } else {
                        CHAT_MANAGER.sendPersistent(moduleName, "Invalid item id: §7minecraft:" + itemName);
                    }
                    break;
                case "del":
                    result = removeFromWhitelist(itemName);
                    if (result) {
                        CHAT_MANAGER.sendPersistent(moduleName, "Removed: §7minecraft:" + itemName);
                    } else {
                        CHAT_MANAGER.sendPersistent(moduleName, "Invalid or not in list: §7minecraft:" + itemName);
                    }
                    break;
                default:
                    CHAT_MANAGER.sendPersistent(moduleName, "Unknown action: §7" + action + ".§f Use §7add/del");
            }
        }

    }
}
