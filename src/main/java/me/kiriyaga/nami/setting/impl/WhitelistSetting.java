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
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class WhitelistSetting extends BoolSetting {
    private final Set<Identifier> whitelist = new HashSet<>();
    private final String moduleName;
    private final String settingName;

    public WhitelistSetting(String name, boolean defaultValue, String moduleName) {
        super(name, defaultValue);
        this.moduleName = moduleName.toLowerCase();
        this.settingName = name.toLowerCase();

        if (COMMAND_MANAGER.getStorage().getCommandByNameOrAlias(this.moduleName.replace(" ", "")) == null) {
            COMMAND_MANAGER.getStorage().addCommand(new WhitelistCommand(this.moduleName));
        }
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

    private static class WhitelistCommand extends Command {
        private final String moduleName;

        public WhitelistCommand(String moduleName) {
            super(moduleName.replace(" ", ""), "Manage list settings for module " + moduleName);
            this.moduleName = moduleName;
        }
        @Override
        public void execute(String[] args) {
            if (args.length < 1) {
                CHAT_MANAGER.sendPersistent(moduleName, "Usage: §7." + moduleName + " <settingName> <add/del/list> [item]");
                return;
            }

            String settingName = args[0].toLowerCase();

            var module = MODULE_MANAGER.getStorage().getByName(moduleName);
            if (module == null) {
                CHAT_MANAGER.sendPersistent(moduleName, "Module '" + moduleName + "' not found.");
                return;
            }

            var setting = module.getSettingByName(settingName);
            if (!(setting instanceof WhitelistSetting listSetting)) {
                CHAT_MANAGER.sendPersistent(moduleName, "Setting '" + settingName + "' not found or not a whitelist setting.");
                return;
            }

            if (args.length < 2) {
                CHAT_MANAGER.sendPersistent(moduleName,
                        "Usage: §7." + moduleName + " " + settingName + " <add/del/list> [item]");
                return;
            }

            String action = args[1].toLowerCase();

            switch (action) {
                case "add" -> {
                    if (args.length < 3) {
                        CHAT_MANAGER.sendPersistent(moduleName,
                                "Usage: §7." + moduleName + " " + settingName + " add <item>");
                        return;
                    }
                    String addItem = args[2].toLowerCase().replace("minecraft:", "");
                    if (listSetting.addToWhitelist(addItem)) {
                        CHAT_MANAGER.sendPersistent(moduleName, "Added: §7minecraft:" + addItem + " to " + settingName);
                    } else {
                        CHAT_MANAGER.sendPersistent(moduleName,
                                "Invalid item id or already added: §7minecraft:" + addItem);
                    }
                }
                case "del" -> {
                    if (args.length < 3) {
                        CHAT_MANAGER.sendPersistent(moduleName,
                                "Usage: §7." + moduleName + " " + settingName + " del <item>");
                        return;
                    }
                    String delItem = args[2].toLowerCase().replace("minecraft:", "");
                    if (listSetting.removeFromWhitelist(delItem)) {
                        CHAT_MANAGER.sendPersistent(moduleName, "Removed: §7minecraft:" + delItem + " from " + settingName);
                    } else {
                        CHAT_MANAGER.sendPersistent(moduleName,
                                "Invalid or not in list: §7minecraft:" + delItem);
                    }
                }
                case "list" -> {
                    if (listSetting.getWhitelist().isEmpty()) {
                        CHAT_MANAGER.sendPersistent(moduleName, settingName + " is empty.");
                        return;
                    }
                    StringBuilder builder = new StringBuilder(settingName + " items: ");
                    int i = 0;
                    for (Identifier id : listSetting.getWhitelist()) {
                        builder.append("§7").append(id.toString()).append("§f");
                        if (i < listSetting.getWhitelist().size() - 1) builder.append(", ");
                        i++;
                    }
                    CHAT_MANAGER.sendPersistent(moduleName, builder.toString());
                }
                default -> CHAT_MANAGER.sendPersistent(moduleName,
                        "Unknown action: §7" + action + ". Use §7add/del/list");
            }
        }
    }
}
