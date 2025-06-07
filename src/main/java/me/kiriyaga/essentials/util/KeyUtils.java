package me.kiriyaga.essentials.util;
import org.lwjgl.glfw.GLFW;

public class KeyUtils {
    public static int parseKey(String keyName) {
        keyName = keyName.toUpperCase();

        try {
            return Integer.parseInt(keyName);
        } catch (NumberFormatException ignored) {}

        switch (keyName) {
            case "LCTRL": return GLFW.GLFW_KEY_LEFT_CONTROL;
            case "RCTRL": return GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LSHIFT": return GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RSHIFT": return GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LALT": return GLFW.GLFW_KEY_LEFT_ALT;
            case "RALT": return GLFW.GLFW_KEY_RIGHT_ALT;
            case "SPACE": return GLFW.GLFW_KEY_SPACE;
            case "ENTER": return GLFW.GLFW_KEY_ENTER;
            case "TAB": return GLFW.GLFW_KEY_TAB;
            case "ESC": case "ESCAPE": return GLFW.GLFW_KEY_ESCAPE;
            case "UP": return GLFW.GLFW_KEY_UP;
            case "DOWN": return GLFW.GLFW_KEY_DOWN;
            case "LEFT": return GLFW.GLFW_KEY_LEFT;
            case "RIGHT": return GLFW.GLFW_KEY_RIGHT;
            case "BACKSPACE": return GLFW.GLFW_KEY_BACKSPACE;
            case "DELETE": return GLFW.GLFW_KEY_DELETE;
            case "INSERT": return GLFW.GLFW_KEY_INSERT;
            case "HOME": return GLFW.GLFW_KEY_HOME;
            case "END": return GLFW.GLFW_KEY_END;
            case "PAGEUP": return GLFW.GLFW_KEY_PAGE_UP;
            case "PAGEDOWN": return GLFW.GLFW_KEY_PAGE_DOWN;
        }

        if (keyName.length() == 1) {
            char c = keyName.charAt(0);
            if (c >= 'A' && c <= 'Z') {
                return GLFW.GLFW_KEY_A + (c - 'A');
            }
            if (c >= '0' && c <= '9') {
                return GLFW.GLFW_KEY_0 + (c - '0');
            }
        }

        return -1;
    }
}
