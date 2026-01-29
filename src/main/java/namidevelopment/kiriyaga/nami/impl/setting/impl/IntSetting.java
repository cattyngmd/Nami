package namidevelopment.kiriyaga.nami.impl.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import namidevelopment.kiriyaga.nami.impl.setting.Setting;

public class IntSetting extends Setting<Integer> {

    private final int min, max;

    public IntSetting(String name, int defaultValue, int min, int max) {
        super(name, name, defaultValue);
        this.min = min;
        this.max = max;
    }

    public IntSetting(String identifier, String name, int defaultValue, int min, int max) {
        super(identifier, name, defaultValue);
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
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
