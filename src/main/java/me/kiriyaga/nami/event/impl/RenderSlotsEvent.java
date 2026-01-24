package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class RenderSlotsEvent extends Event {

    private final GuiGraphics graphics;
    private final int mouseX;
    private final int mouseY;
    private final List<Slot> slots;

    public RenderSlotsEvent(GuiGraphics graphics, int mouseX, int mouseY, List<Slot> slots) {
        this.graphics = graphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.slots = slots;
    }

    public GuiGraphics graphics() {
        return graphics;
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    public List<Slot> slots() {
        return slots;
    }
}
