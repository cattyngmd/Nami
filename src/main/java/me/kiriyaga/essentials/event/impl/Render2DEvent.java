package me.kiriyaga.essentials.event.impl;

import me.kiriyaga.essentials.event.Event;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class Render2DEvent extends Event {
    private final DrawContext drawContext;
    private final RenderTickCounter renderTickCounter;

    public Render2DEvent(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        this.drawContext = drawContext;
        this.renderTickCounter = renderTickCounter;
    }

    public DrawContext getDrawContext() {
        return drawContext;
    }

    public RenderTickCounter getRenderTickCounter() {
        return renderTickCounter;
    }
}
