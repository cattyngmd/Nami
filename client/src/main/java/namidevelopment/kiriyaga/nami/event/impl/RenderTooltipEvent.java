package namidevelopment.kiriyaga.nami.event.impl;

import namidevelopment.kiriyaga.nami.event.Event;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class RenderTooltipEvent extends Event {

    private final GuiGraphics graphics;
    private final int mouseX, mouseY;
    private final ItemStack hoveredStack;

    public RenderTooltipEvent(GuiGraphics graphics, int mouseX, int mouseY, ItemStack hoveredStack) {
        this.graphics = graphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.hoveredStack = hoveredStack;
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

    public ItemStack hoveredStack() {
        return hoveredStack;
    }
}
