package namidevelopment.kiriyaga.api.model.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import namidevelopment.kiriyaga.api.util.KeyUtils;
import org.lwjgl.glfw.GLFW;

import static namidevelopment.kiriyaga.api.NamiApi.MC;

public class KeyBindSetting extends Setting<Integer> {

    public static final int KEY_NONE = -1;
    private boolean wasPressedLastTick = false;
    private boolean holdMode = false;

    public KeyBindSetting(String identifier, String name, int defaultKey) {
        super(identifier, name, defaultKey);
    }

    public KeyBindSetting(String identifier, String name, String defaultKeyName) {
        this(identifier, name, defaultKeyName != null ? KeyUtils.parseKey(defaultKeyName) : KEY_NONE);
    }

    public KeyBindSetting(String name) {
        this(name, name, KEY_NONE);
    }

    public KeyBindSetting(String name, int defaultKey) {
        this(name, name, defaultKey);
    }

    public KeyBindSetting(String name, String defaultKeyName) {
        this(name, name, defaultKeyName);
    }


    public void setDefaultKey(String keyName) {
        this.value = KeyUtils.parseKey(keyName);
    }

    public void setDefaultKey(int keyCode) {
        this.value = keyCode;
    }


    public boolean isPressed() {
        if (value == KEY_NONE) return false;

        long window = MC.getWindow().handle();

        if (value >= GLFW.GLFW_MOUSE_BUTTON_1 && value <= GLFW.GLFW_MOUSE_BUTTON_8) {
            return GLFW.glfwGetMouseButton(window, value) == GLFW.GLFW_PRESS;
        }

        return GLFW.glfwGetKey(window, value) == GLFW.GLFW_PRESS;
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
        JsonObject obj = new JsonObject();
        obj.addProperty("value", value);
        obj.addProperty("holdMode", holdMode);
        return obj;
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            this.value = obj.has("value") ? obj.get("value").getAsInt() : KEY_NONE;
            this.holdMode = obj.has("holdMode") && obj.get("holdMode").getAsBoolean();
        } else {
            this.value = KEY_NONE;
            this.holdMode = false;
        }
    }
}
