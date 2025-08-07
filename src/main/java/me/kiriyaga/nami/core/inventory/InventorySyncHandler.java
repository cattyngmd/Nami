package me.kiriyaga.nami.core.inventory;

import me.kiriyaga.nami.core.inventory.model.PreSwapEntry;
import me.kiriyaga.nami.event.*;
import me.kiriyaga.nami.event.impl.EntityDeathEvent;
import me.kiriyaga.nami.event.impl.ItemEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.impl.client.Debug;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;

import static me.kiriyaga.nami.Nami.*;

public class InventorySyncHandler {

    private final InventorySlotHandler slotSwapper;

    public InventorySyncHandler(InventorySlotHandler swapper) {
        this.slotSwapper = swapper;
    }

    private boolean debugInventoryEnabled() {
        return MODULE_MANAGER.getStorage().getByClass(Debug.class).isEnabled()
                && MODULE_MANAGER.getStorage().getByClass(Debug.class).inventory.get();
    }

    public void init() {
        if (debugInventoryEnabled()) {
            CHAT_MANAGER.sendRaw("[InventorySyncHandler] Initializing");
        }
        EVENT_MANAGER.register(this);
    }

    public void swapSync() {
        if (!slotSwapper.isOutOfSync()) {
            if (debugInventoryEnabled()) {
                CHAT_MANAGER.sendRaw("[swapSync] No desync detected. Skipping sync.");
            }
            return;
        }

        int clientSlot = MC.player.getInventory().getSelectedSlot();
        if (debugInventoryEnabled()) {
            CHAT_MANAGER.sendRaw("[swapSync] Desync detected. Client slot: " + clientSlot + ", Synced slot: " + slotSwapper.getSyncedSlot());
        }

        slotSwapper.sendSlotPacket(clientSlot);

        for (PreSwapEntry swapData : slotSwapper.getSwaps()) {
            if (debugInventoryEnabled()) {
                CHAT_MANAGER.sendRaw("[swapSync] Marking swap entry for clear: " + swapData);
            }
            swapData.markForClear();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemSync(ItemEvent event) {
        if (slotSwapper.isOutOfSync()) {
            if (debugInventoryEnabled()) {
                CHAT_MANAGER.sendRaw("[onItemSync] Cancelling item event due to desync.");
            }
            event.cancel();

            ItemStack serverStack = getCurrentServerStack();
            event.setStack(serverStack);

            if (debugInventoryEnabled()) {
                CHAT_MANAGER.sendRaw("[onItemSync] Forcing server stack: " + serverStack);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(EntityDeathEvent event) {
        if (event.getLivingEntity() == MC.player && MC.player != null) {
            if (debugInventoryEnabled()) {
                CHAT_MANAGER.sendRaw("[onPlayerDeath] Player died. Syncing slot and clearing swaps.");
            }
            slotSwapper.sendSlotPacket(MC.player.getInventory().getSelectedSlot());
            slotSwapper.markAllForClear();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreTick(PreTickEvent event) {
        swapSync();

        int before = slotSwapper.getSwaps().size();
        slotSwapper.getSwaps().removeIf(PreSwapEntry::isExpired);
        int after = slotSwapper.getSwaps().size();

        if (debugInventoryEnabled()) {
            CHAT_MANAGER.sendRaw("[onPreTick] Cleaned expired swaps. Before: " + before + ", After: " + after);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof UpdateSelectedSlotS2CPacket updateSlot) {
            int serverSlot = updateSlot.comp_3325();
            if (debugInventoryEnabled()) {
                CHAT_MANAGER.sendRaw("[onReceive] Received server slot update: " + serverSlot);
            }
            slotSwapper.sendSlotPacket(serverSlot);
        }

        if (packet instanceof ScreenHandlerSlotUpdateS2CPacket update) {
            int slot = update.getSlot();
            int hotbarSlot = slot - 36;

            if (hotbarSlot < 0 || hotbarSlot > 8 || update.getStack().isEmpty()) return;

            if (debugInventoryEnabled()) {
                CHAT_MANAGER.sendRaw("[onReceive] SlotUpdate packet received for hotbar slot: " + hotbarSlot + ", item: " + update.getStack());
            }

            for (PreSwapEntry entry : slotSwapper.getSwaps()) {
                if (entry.involvesSlot(hotbarSlot)) {
                    ItemStack expected = entry.getSnapshotItem(hotbarSlot);
                    ItemStack received = update.getStack();

                    if (!expected.getItem().equals(received.getItem())) {
                        if (debugInventoryEnabled()) {
                            CHAT_MANAGER.sendRaw("[onReceive] Cancelling slot update packet! Expected: " + expected + ", Received: " + received);
                        }
                        event.cancel();
                        break;
                    } else {
                        if (debugInventoryEnabled()) {
                            CHAT_MANAGER.sendRaw("[onReceive] Slot update matches expected swap entry.");
                        }
                    }
                }
            }
        }
    }

    private ItemStack getCurrentServerStack() {
        int serverSlot = slotSwapper.getSyncedSlot();
        ItemStack stack = (MC.player != null && serverSlot != -1)
                ? MC.player.getInventory().getStack(serverSlot)
                : ItemStack.EMPTY;

        if (debugInventoryEnabled()) {
            CHAT_MANAGER.sendRaw("[getCurrentServerStack] Synced slot: " + serverSlot + ", Stack: " + stack);
        }
        return stack;
    }
}
