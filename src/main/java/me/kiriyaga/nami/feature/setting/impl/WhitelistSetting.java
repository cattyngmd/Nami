package me.kiriyaga.nami.feature.setting.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.kiriyaga.nami.feature.command.impl.WhitelistCommand;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;

public class WhitelistSetting extends BoolSetting {
    private final Set<Identifier> whitelist = new HashSet<>();
    private final String moduleName;
    private final String settingName;

    public WhitelistSetting(String name, boolean defaultValue, String moduleName) {
        super(name, defaultValue);
        this.moduleName = moduleName.toLowerCase();
        this.settingName = name.toLowerCase();

        // The WhitelistCommand is now registered centrally in CommandRegistry.
        // This dynamic registration is removed to prevent duplicates and conflicts.
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
}
