package me.kiriyaga.nami.core.breaking;

public class BreakStateHandler {
    private boolean breaking = false;
    private boolean breakingThisTick = false;

    public void resetThisTick() {
        breakingThisTick = false;
    }

    public void confirmBreaking() {
        breaking = true;
        breakingThisTick = true;
    }

    public boolean shouldCancelBreak() {
        return !breakingThisTick && breaking;
    }

    public void cancelBreaking() {
        breaking = false;
    }

    public boolean isBreaking() {
        return breaking;
    }
}
