package me.kiriyaga.essentials.event.events;

import me.kiriyaga.essentials.event.Event;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class Render2DEvent extends Event {
    private final DrawContext drawContext;
    private final float tickDelta;

    public Render2DEvent(DrawContext drawContext, float tickDelta) {
        this.drawContext = drawContext;
        this.tickDelta = tickDelta;
    }

    public DrawContext getDrawContext() {
        return drawContext;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
