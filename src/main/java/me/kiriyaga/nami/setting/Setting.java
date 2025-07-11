package me.kiriyaga.nami.setting;

import com.google.gson.JsonElement;

public abstract class Setting<T> {
    protected final String name;
    protected T value;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public abstract void fromJson(JsonElement json);
    public abstract JsonElement toJson();
}
