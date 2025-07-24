package me.kiriyaga.nami.core.inventory;

import me.kiriyaga.nami.event.*;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static me.kiriyaga.nami.Nami.*;

public class InventorySlotHandler {

    private final List<PreSwapEntry> swaps = new CopyOnWriteArrayList<>();
    private int syncedSlot = -1;

    public void init() {
        EVENT_MANAGER.register(this);
    }

    public void attemptSwitch(int targetSlot) {
        if (syncedSlot != targetSlot && PlayerInventory.isValidHotbarIndex(targetSlot)) {
            sendSlotPacket(targetSlot);

            ItemStack[] snapshot = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                snapshot[i] = MC.player.getInventory().getStack(i);
            }

            swaps.add(new PreSwapEntry(snapshot, syncedSlot, targetSlot));
        }
    }

    public void forceClientSlot(int slot) {
        if (MC.player.getInventory().getSelectedSlot() != slot) {
            MC.player.getInventory().setSelectedSlot(slot);
            sendSlotPacket(slot);
        }
    }

    public void sendSlotPacket(int slot) {
        MC.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }

    public int getSyncedSlot() {
        return syncedSlot;
    }

    public boolean isOutOfSync() {
        return MC.player.getInventory().getSelectedSlot() != syncedSlot;
    }

    public List<PreSwapEntry> getSwaps() {
        return swaps;
    }

    public void markAllForClear() {
        swaps.forEach(PreSwapEntry::markForClear);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSend(PacketSendEvent event) {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet) {
            int slot = packet.getSelectedSlot();
            if (!PlayerInventory.isValidHotbarIndex(slot) || syncedSlot == slot) {
                event.cancel();
            } else {
                syncedSlot = slot;
            }
        }
    }
}
