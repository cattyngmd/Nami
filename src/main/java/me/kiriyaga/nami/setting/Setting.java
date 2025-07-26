package me.kiriyaga.nami.setting;

import com.google.gson.JsonElement;

public abstract class Setting<T> {
    protected final String name;
    protected T value;
    private Runnable onChanged = null;
    private boolean show = true;

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
        if (onChanged != null) onChanged.run();
    }

    public void setOnChanged(Runnable callback) {
        this.onChanged = callback;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public abstract void fromJson(JsonElement json);
    public abstract JsonElement toJson();
}
