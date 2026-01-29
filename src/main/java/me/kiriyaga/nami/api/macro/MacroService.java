package me.kiriyaga.nami.api.macro;

import me.kiriyaga.nami.api.macro.model.Macro;

import java.util.*;

import static me.kiriyaga.nami.Nami.MC;

public class MacroService {
    private final List<Macro> macros = new ArrayList<>();

    private final Map<Integer, Boolean> lastKeyStates = new HashMap<>();

    public boolean isKeyPressed(int keyCode) {
        return org.lwjgl.glfw.GLFW.glfwGetKey(MC.getWindow().handle(), keyCode) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    public boolean wasKeyPressedLastTick(int keyCode) {
        return lastKeyStates.getOrDefault(keyCode, false);
    }

    public void setKeyPressedLastTick(int keyCode, boolean pressed) {
        lastKeyStates.put(keyCode, pressed);
    }

    public void addMacro(Macro macro) {
        macros.add(macro);
    }

    public void removeMacro(int keyCode) {
        macros.removeIf(m -> m.getKeyCode() == keyCode);
    }

    public Macro getMacro(int keyCode) {
        for (Macro m : macros) {
            if (m.getKeyCode() == keyCode) return m;
        }
        return null;
    }

    public List<Macro> getAll() {
        return macros;
    }

    public void clear() {
        macros.clear();
    }
}
