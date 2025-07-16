package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

import java.util.*;

import static me.kiriyaga.nami.Nami.*;

public class ElytraSwapModule extends Module {

    private final BoolSetting fastSwap = addSetting(new BoolSetting("fast swap", false));
    private final IntSetting swapSlotSetting = addSetting(new IntSetting("swap slot", 8 , 1, 9));

    private static final int ARMOR_CHEST_SLOT = 6;
    private static final Map<Item, Integer> CHESTPLATE_PRIORITY = Map.of(
            Items.LEATHER_CHESTPLATE, 1,
            Items.GOLDEN_CHESTPLATE, 2,
            Items.CHAINMAIL_CHESTPLATE, 3,
            Items.IRON_CHESTPLATE, 4,
            Items.DIAMOND_CHESTPLATE, 5,
            Items.NETHERITE_CHESTPLATE, 6
    );

    public ElytraSwapModule() {
        super("elytra swap", "Swaps elytra with the chestplate automatically.", Category.world, "elytraswap");
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPostTick(PostTickEvent event) {
        if (MINECRAFT.world == null || MINECRAFT.player == null) return;

        if (fastSwap.get() && (MINECRAFT.currentScreen == null || MINECRAFT.currentScreen instanceof InventoryScreen)) {
            attemptFastSwap();
        } else if (MINECRAFT.currentScreen == null || MINECRAFT.currentScreen instanceof InventoryScreen) {
            attemptSwap();
        }
    }

    private void attemptFastSwap() {
        ClientPlayerEntity player = MINECRAFT.player;
        if (player == null) return;

        int syncId = player.currentScreenHandler.syncId;
        int hotbarSlotIndex = swapSlotSetting.get() - 1;
        ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack swapStack = player.getInventory().getStack(hotbarSlotIndex);

        if (swapStack.isEmpty()) return;

        if (chestItem.getItem() == Items.ELYTRA) {
            if (CHESTPLATE_PRIORITY.containsKey(swapStack.getItem())) {
                MINECRAFT.interactionManager.clickSlot(syncId, ARMOR_CHEST_SLOT, hotbarSlotIndex, SlotActionType.SWAP, player);
                this.setEnabled(false);
                CHAT_MANAGER.sendTransient("Elytra swapped.");
            }
        } else {
            if (swapStack.getItem() == Items.ELYTRA) {
                MINECRAFT.interactionManager.clickSlot(syncId, ARMOR_CHEST_SLOT, hotbarSlotIndex, SlotActionType.SWAP, player);
                this.setEnabled(false);
                CHAT_MANAGER.sendTransient("Elytra swapped.");
            }
        }
    }



    private void attemptSwap() {
        ClientPlayerEntity player = MINECRAFT.player;
        int syncId = player.currentScreenHandler.syncId;
        ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);

        if (chestItem.getItem() == Items.ELYTRA) {
            Item bestChestplate = findBestChestplateInInventory();
            if (bestChestplate != null) {
                int slot = findChestplateSlot(bestChestplate);
                if (slot != -1) {
                    MINECRAFT.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, player);
                    MINECRAFT.interactionManager.clickSlot(syncId, ARMOR_CHEST_SLOT, 0, SlotActionType.PICKUP, player);
                    MINECRAFT.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, player);
                    this.setEnabled(false);
                    CHAT_MANAGER.sendTransient("Elytra swapped.");
                }
            }
        } else {
            int elytraSlot = findElytraSlot();
            if (elytraSlot != -1) {
                MINECRAFT.interactionManager.clickSlot(syncId, elytraSlot, 0, SlotActionType.PICKUP, player);
                MINECRAFT.interactionManager.clickSlot(syncId, ARMOR_CHEST_SLOT, 0, SlotActionType.PICKUP, player);
                MINECRAFT.interactionManager.clickSlot(syncId, elytraSlot, 0, SlotActionType.PICKUP, player);
                this.setEnabled(false);
                CHAT_MANAGER.sendTransient("Elytra swapped.");
            }
        }
    }

    private int findElytraSlot() {
        ClientPlayerEntity player = MINECRAFT.player;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == Items.ELYTRA) {
                return convertSlot(i);
            }
        }
        return -1;
    }

    private Item findBestChestplateInInventory() {
        ClientPlayerEntity player = MINECRAFT.player;
        Item best = null;
        int bestPriority = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack != null && CHESTPLATE_PRIORITY.containsKey(stack.getItem())) {
                int priority = CHESTPLATE_PRIORITY.get(stack.getItem());
                if (priority > bestPriority) {
                    bestPriority = priority;
                    best = stack.getItem();
                }
            }
        }
        return best;
    }

    private int findChestplateSlot(Item chestplate) {
        ClientPlayerEntity player = MINECRAFT.player;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == chestplate) {
                return convertSlot(i);
            }
        }
        return -1;
    }

    private int convertSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }
}