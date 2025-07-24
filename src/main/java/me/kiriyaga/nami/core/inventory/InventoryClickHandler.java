package me.kiriyaga.nami.core.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.util.collection.DefaultedList;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.List;

import static me.kiriyaga.nami.Nami.MC;

public class InventoryClickHandler {

    public int pickupSlot(int slotIndex) {
        return click(slotIndex, 0, SlotActionType.PICKUP);
    }

    public void quickMoveSlot(int slotIndex) {
        click(slotIndex, 0, SlotActionType.QUICK_MOVE);
    }

    public void throwSlot(int slotIndex) {
        click(slotIndex, 0, SlotActionType.THROW);
    }

    public void swapSlot(int targetSlot, int hotbarSlotIndex) {
        click(targetSlot, hotbarSlotIndex, SlotActionType.SWAP);
    }

    private int click(int slot, int button, SlotActionType type) {
        if (slot < 0) return -1;

        ScreenHandler handler = MC.player.currentScreenHandler;
        DefaultedList<Slot> slots = handler.slots;
        List<ItemStack> before = Lists.newArrayListWithCapacity(slots.size());

        for (Slot s : slots) before.add(s.getStack().copy());

        handler.onSlotClick(slot, button, type, MC.player);

        Int2ObjectMap<ItemStackHash> changes = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < slots.size(); i++) {
            if (!ItemStack.areEqual(before.get(i), slots.get(i).getStack())) {
                changes.put(i, ItemStackHash.fromItemStack(slots.get(i).getStack().copy(), c -> 0));
            }
        }

        MC.player.networkHandler.sendPacket(new ClickSlotC2SPacket(
                handler.syncId,
                handler.getRevision(),
                (short) slot,
                (byte) button,
                type,
                changes,
                ItemStackHash.fromItemStack(handler.getCursorStack().copy(), c -> 0)
        ));

        return handler.getRevision();
    }
}
