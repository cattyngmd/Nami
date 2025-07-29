package me.kiriyaga.nami.core.inventory;

import me.kiriyaga.nami.util.Timer;
import net.minecraft.item.ItemStack;

public class PreSwapEntry {

    private final ItemStack[] snapshot;
    private final int fromSlot, toSlot;
    private final Timer timer = new Timer();

    public PreSwapEntry(ItemStack[] snapshot, int from, int to) {
        this.snapshot = snapshot;
        this.fromSlot = from;
        this.toSlot = to;
    }

    public boolean involvesSlot(int index) {
        return index == fromSlot || index == toSlot;
    }

    public ItemStack getSnapshotItem(int index) {
        return snapshot[index];
    }

    public boolean isExpired() {
        return timer.hasElapsed(300);
    }

    public void markForClear() {
        timer.reset();
    }
}
