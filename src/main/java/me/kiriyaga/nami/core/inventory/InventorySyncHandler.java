package me.kiriyaga.nami.core.inventory;

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

    public void init() {
        EVENT_MANAGER.register(this);
    }

    public void swapSync() {
        if (!slotSwapper.isOutOfSync())
            return;

        slotSwapper.sendSlotPacket(MC.player.getInventory().getSelectedSlot());
        for (PreSwapEntry swapData : slotSwapper.getSwaps()) {
            if (MODULE_MANAGER.getStorage().getByClass(Debug.class).isEnabled() && MODULE_MANAGER.getStorage().getByClass(Debug.class).inventory.get())
                CHAT_MANAGER.sendRaw("marking entry for clear due to out of sync");
            swapData.markForClear();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemSync(ItemEvent event) {
        if (slotSwapper.isOutOfSync()) {
            if (MODULE_MANAGER.getStorage().getByClass(Debug.class).isEnabled() && MODULE_MANAGER.getStorage().getByClass(Debug.class).inventory.get())
                CHAT_MANAGER.sendRaw("cancelling item sync due to desync");
            event.cancel();
            event.setStack(getCurrentServerStack());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(EntityDeathEvent event) {
        if (event.getLivingEntity() == MC.player && MC.player != null) {
            slotSwapper.sendSlotPacket(MC.player.getInventory().getSelectedSlot());
            slotSwapper.markAllForClear();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreTick(PreTickEvent event) {
        swapSync();
        slotSwapper.getSwaps().removeIf(PreSwapEntry::isExpired);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof UpdateSelectedSlotS2CPacket updateSlot) {
            slotSwapper.sendSlotPacket(updateSlot.comp_3325());
        }

        if (packet instanceof ScreenHandlerSlotUpdateS2CPacket update) {
            int hotbarSlot = update.getSlot() - 36;
            if (hotbarSlot < 0 || hotbarSlot > 8 || update.getStack().isEmpty()) return;

            for (PreSwapEntry entry : slotSwapper.getSwaps()) {
                if (entry.involvesSlot(hotbarSlot)
                        && !entry.getSnapshotItem(hotbarSlot).getItem().equals(update.getStack().getItem())) {
                    if (MODULE_MANAGER.getStorage().getByClass(Debug.class).isEnabled() && MODULE_MANAGER.getStorage().getByClass(Debug.class).inventory.get())
                        CHAT_MANAGER.sendRaw("packet recieved: desync detected " + hotbarSlot);
                    event.cancel();
                    break;
                }
            }
        }
    }

    private ItemStack getCurrentServerStack() {
        int serverSlot = slotSwapper.getSyncedSlot();
        return (MC.player != null && serverSlot != -1)
                ? MC.player.getInventory().getStack(serverSlot)
                : ItemStack.EMPTY;
    }
}
