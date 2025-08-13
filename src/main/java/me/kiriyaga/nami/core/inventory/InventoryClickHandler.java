package me.kiriyaga.nami.core.inventory;

import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

import com.google.common.collect.Lists;

import java.util.List;

import static me.kiriyaga.nami.Nami.MC;

public class InventoryClickHandler {

    public void pickupSlot(int slotIndex) {
        click(slotIndex, 0, SlotActionType.PICKUP);
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

    private void click(int slot, int button, SlotActionType type) {
        if (slot < 0) return;

        if (MC.currentScreen instanceof GenericContainerScreen
                || MC.currentScreen instanceof AnvilScreen
                || MC.currentScreen instanceof AbstractCommandBlockScreen
                || MC.currentScreen instanceof StructureBlockScreen
                || MC.currentScreen instanceof CreativeInventoryScreen) {
            return;
        }

        ScreenHandler handler = MC.player.currentScreenHandler;
        DefaultedList<Slot> slots = handler.slots;
        List<ItemStack> before = Lists.newArrayListWithCapacity(slots.size());
        for (Slot s : slots) before.add(s.getStack().copy());

        MC.interactionManager.clickSlot(handler.syncId, slot, button, type, MC.player);
    }
}