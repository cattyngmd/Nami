package me.kiriyaga.essentials.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import me.kiriyaga.essentials.setting.Setting;
import org.lwjgl.glfw.GLFW;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class KeyBindSetting extends Setting<Integer> {

    public static final int KEY_NONE = -1;
    private boolean wasPressedLastTick = false;
    private boolean holdMode = false;

    public KeyBindSetting(String name, int defaultKey) {
        super(name, defaultKey);
    }

    public boolean isPressed() {
        if (value == KEY_NONE) return false;
        return GLFW.glfwGetKey(MINECRAFT.getWindow().getHandle(), value) == GLFW.GLFW_PRESS;
    }

    public String getKeyName() {
        if (value == KEY_NONE) return "None";
        return GLFW.glfwGetKeyName(value, 0);
    }

    public boolean isHoldMode() {
        return holdMode;
    }

    public void setHoldMode(boolean holdMode) {
        this.holdMode = holdMode;
    }


    public boolean wasPressedLastTick() {
        return wasPressedLastTick;
    }

    public void setWasPressedLastTick(boolean val) {
        this.wasPressedLastTick = val;
    }

    @Override
    public void set(Integer value) {
        this.value = value;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            this.value = json.getAsInt();
        } else {
            this.value = KEY_NONE;
        }
    }
}
