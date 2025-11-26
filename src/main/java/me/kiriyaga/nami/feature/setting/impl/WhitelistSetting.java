package me.kiriyaga.nami.feature.setting.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WhitelistSetting extends BoolSetting {

    public enum Type {
        ANY, BLOCK, ITEM, ENTITY, SOUND, PARTICLE, STRING
    }

    private final Set<Identifier> whitelist = new HashSet<>();
    private final Set<String> stringWhitelist = new HashSet<>();
    private final Set<Type> allowedTypes = new HashSet<>();

    public WhitelistSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
        this.allowedTypes.add(Type.ANY);
    }

    public WhitelistSetting(String name, boolean defaultValue, Type... types) {
        this(name, defaultValue);
        this.allowedTypes.clear();
        if (types == null || types.length == 0) {
            this.allowedTypes.add(Type.ANY);
        } else {
            this.allowedTypes.addAll(Arrays.asList(types));
        }
    }

    public Set<Type> getAllowedTypes() {
        return Set.copyOf(allowedTypes);
    }

    public boolean allows(Type t) {
        return allowedTypes.contains(Type.ANY) || allowedTypes.contains(t);
    }

    public Set<Identifier> getWhitelist() {
        return whitelist;
    }

    public boolean isWhitelisted(Identifier id) {
        return whitelist.contains(id);
    }

    public boolean addToWhitelist(String idStr) {
        Identifier id = Identifier.tryParse(idStr);
        if (id == null) return false;
        return whitelist.add(id);
    }

    public boolean removeFromWhitelist(String idStr) {
        Identifier id = Identifier.tryParse(idStr);
        if (id == null) return false;
        return whitelist.remove(id);
    }
    public boolean addString(String s) {
        return stringWhitelist.add(s.toLowerCase());
    }

    public boolean removeString(String s) {
        return stringWhitelist.remove(s.toLowerCase());
    }

    public boolean isStringWhitelisted(String s) {
        return stringWhitelist.contains(s.toLowerCase());
    }

    public Set<String> getStringWhitelist() {
        return Set.copyOf(stringWhitelist);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject()) return;
        JsonObject obj = json.getAsJsonObject();

        if (obj.has("enabled"))
            this.value = obj.get("enabled").getAsBoolean();

        if (obj.has("items") && obj.get("items").isJsonArray()) {
            whitelist.clear();
            for (JsonElement e : obj.getAsJsonArray("items")) {
                Identifier id = Identifier.tryParse(e.getAsString());
                if (id != null) whitelist.add(id);
            }
        }

        if (obj.has("strings") && obj.get("strings").isJsonArray()) {
            stringWhitelist.clear();
            for (JsonElement e : obj.getAsJsonArray("strings")) {
                stringWhitelist.add(e.getAsString().toLowerCase());
            }
        }

        if (obj.has("types")) {
            allowedTypes.clear();
            for (JsonElement e : obj.getAsJsonArray("types")) {
                try {
                    allowedTypes.add(Type.valueOf(e.getAsString().toUpperCase()));
                } catch (Exception ignored) {}
            }
            if (allowedTypes.isEmpty()) allowedTypes.add(Type.ANY);
        }
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("enabled", value);

        JsonArray items = new JsonArray();
        whitelist.forEach(id -> items.add(id.toString()));
        obj.add("items", items);

        JsonArray strings = new JsonArray();
        stringWhitelist.forEach(strings::add);
        obj.add("strings", strings);

        JsonArray types = new JsonArray();
        allowedTypes.forEach(t -> types.add(t.name()));
        obj.add("types", types);

        return obj;
    }
}
