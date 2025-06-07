package me.kiriyaga.essentials.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import me.kiriyaga.essentials.setting.Setting;

public class IntSetting extends Setting<Integer> {

    private final int min, max;

    public IntSetting(String name, int defaultValue, int min, int max) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    public void set(Integer value) {
        this.value = Math.max(min, Math.min(max, value));
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            this.value = json.getAsInt();
        }
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }
}
