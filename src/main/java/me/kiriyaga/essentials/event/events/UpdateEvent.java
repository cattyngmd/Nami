package me.kiriyaga.essentials.event.events;

import me.kiriyaga.essentials.event.Event;

public class UpdateEvent extends Event {
    private final float partialTicks;

    public UpdateEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
