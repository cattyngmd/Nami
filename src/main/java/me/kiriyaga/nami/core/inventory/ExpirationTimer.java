package me.kiriyaga.nami.core.inventory;

public class ExpirationTimer {
    private long start = System.currentTimeMillis();

    public void reset() {
        start = System.currentTimeMillis();
    }

    public boolean hasElapsed(long ms) {
        return System.currentTimeMillis() - start >= ms;
    }
}
