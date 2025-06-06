package me.kiriyaga.essentials.feature.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import me.kiriyaga.essentials.feature.setting.Setting;

public class DoubleSetting extends Setting<Double> {

    private final double min, max;

    public DoubleSetting(String name, double defaultValue, double min, double max) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    @Override
    public void set(Double value) {
        this.value = Math.max(min, Math.min(max, value));
    }

    public void fromJson(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            set(json.getAsDouble());
        }
    }

    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }
}
