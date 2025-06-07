package me.kiriyaga.essentials.event.impl;

import me.kiriyaga.essentials.event.Event;
import net.minecraft.client.util.math.MatrixStack;

public class Render3DEvent extends Event {
    private final MatrixStack matrixStack;
    private final float partialTicks;

    public Render3DEvent(MatrixStack matrixStack, float partialTicks) {
        this.matrixStack = matrixStack;
        this.partialTicks = partialTicks;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
