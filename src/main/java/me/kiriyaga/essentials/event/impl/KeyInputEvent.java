package me.kiriyaga.essentials.event.impl;

import me.kiriyaga.essentials.event.Event;

public class KeyInputEvent extends Event {
    public final int key;
    public final int scancode;
    public final int action;
    public final int modifiers;

    public KeyInputEvent(int key, int scancode, int action, int modifiers) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.modifiers = modifiers;
    }
}
