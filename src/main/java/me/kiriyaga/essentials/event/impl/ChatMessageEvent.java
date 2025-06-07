package me.kiriyaga.essentials.event.impl;

import me.kiriyaga.essentials.event.Event;

public class ChatMessageEvent extends Event {
    private final String message;
    private boolean cancelled;

    public ChatMessageEvent(String message) {
        this.message = message;
        this.cancelled = false;
    }

    public String getMessage() {
        return message;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
