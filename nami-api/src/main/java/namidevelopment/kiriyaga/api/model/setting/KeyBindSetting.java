package namidevelopment.kiriyaga.api.model.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import namidevelopment.kiriyaga.api.util.KeyUtils;
import org.lwjgl.glfw.GLFW;

import static namidevelopment.kiriyaga.api.NamiApi.API_MC;

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

        if (value == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return API_MC.mouseHandler.isLeftPressed();
        }
        if (value == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            return API_MC.mouseHandler.isRightPressed();
        }
        if (value == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            return API_MC.mouseHandler.isMiddlePressed();
        }

        return GLFW.glfwGetKey(API_MC.getWindow().handle(), value) == GLFW.GLFW_PRESS;
    }

    public String getKeyName() {
        if (value == KEY_NONE) return "none";

        switch (value) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT: return "MOUSE_LEFT";
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT: return "MOUSE_RIGHT";
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE: return "MOUSE_MIDDLE";
            case GLFW.GLFW_MOUSE_BUTTON_4: return "MOUSE_4";
            case GLFW.GLFW_MOUSE_BUTTON_5: return "MOUSE_5";
            case GLFW.GLFW_MOUSE_BUTTON_6: return "MOUSE_6";
            case GLFW.GLFW_MOUSE_BUTTON_7: return "MOUSE_7";
            case GLFW.GLFW_MOUSE_BUTTON_8: return "MOUSE_8";
        }

        String keyName = GLFW.glfwGetKeyName(value, 0);
        return keyName != null ? keyName.toUpperCase() : "KEY_" + value;
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
