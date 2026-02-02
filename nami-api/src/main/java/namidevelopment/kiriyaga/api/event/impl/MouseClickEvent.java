package namidevelopment.kiriyaga.api.event.impl;

import namidevelopment.kiriyaga.api.event.Event;

public class MouseClickEvent extends Event {
    private final double mouseX;
    private final double mouseY;
    private final int button;

    public MouseClickEvent(double mouseX, double mouseY, int button) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.button = button;
    }

    public double mouseX() {
        return mouseX;
    }

    public double mouseY() {
        return mouseY;
    }

    public int button() {
        return button;
    }
}
