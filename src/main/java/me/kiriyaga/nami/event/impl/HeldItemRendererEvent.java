package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;

public class HeldItemRendererEvent extends Event {

    private final Hand hand;
    private final MatrixStack matrix;

    public HeldItemRendererEvent(Hand hand, MatrixStack matrices) {
        this.hand = hand;
        this.matrix = matrices;
    }

    public Hand getHand() {
        return hand;
    }

    public MatrixStack getMatrix() {
        return matrix;
    }
}