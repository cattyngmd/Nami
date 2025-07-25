package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

import java.util.*;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class ElytraSwapModule extends Module {
    private static final int ARMOR_CHEST_SLOT = 2;
    private static final Map<Item, Integer> CHESTPLATE_PRIORITY = Map.of(
            Items.LEATHER_CHESTPLATE, 1,
            Items.GOLDEN_CHESTPLATE, 2,
            Items.CHAINMAIL_CHESTPLATE, 3,
            Items.IRON_CHESTPLATE, 4,
            Items.DIAMOND_CHESTPLATE, 5,
            Items.NETHERITE_CHESTPLATE, 6
    );

    public ElytraSwapModule() {
        super("elytra swap", "Swaps elytra with the chestplate automatically.", ModuleCategory.of("world"), "elytraswap");
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPostTick(PostTickEvent event) {
        if (MC.world == null || MC.player == null) return;

        if (MC.currentScreen == null || MC.currentScreen instanceof InventoryScreen) {
            attemptSwap();
        }
    }

    private void attemptSwap() {
        ClientPlayerEntity player = MC.player;
        ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);

        if (chestItem.getItem() == Items.ELYTRA) {
            Item bestChestplate = findBestChestplateInInventory();
            if (bestChestplate != null) {
                int slot = findChestplateSlot(bestChestplate);
                if (slot != -1) {
                    swapArmor(ARMOR_CHEST_SLOT, slot);
                    this.setEnabled(false);
                    CHAT_MANAGER.sendTransient(CAT_FORMAT.format("swapped: {g}chest{reset}."));
                }
            }
        } else {
            int elytraSlot = findElytraSlot();
            if (elytraSlot != -1) {
                swapArmor(ARMOR_CHEST_SLOT, elytraSlot);
                this.setEnabled(false);
                CHAT_MANAGER.sendTransient(CAT_FORMAT.format("swapped: {g}elytra{reset}."));
            }
        }
    }

    public void swapArmor(int armorSlot, int slot) {
        ClientPlayerEntity player = MC.player;

        ItemStack armorStack = player.getEquippedStack(getArmorEquipmentSlot(armorSlot));
        int realSlot = slot < 9 ? slot + 36 : slot;
        int armorSlotCorrected = 8 - armorSlot;

        INVENTORY_MANAGER.getClickHandler().pickupSlot(realSlot);
        boolean hasArmor = !armorStack.isEmpty();
        INVENTORY_MANAGER.getClickHandler().pickupSlot(armorSlotCorrected);
        if (hasArmor) {
            INVENTORY_MANAGER.getClickHandler().pickupSlot(realSlot);
        }
    }

    private EquipmentSlot getArmorEquipmentSlot(int armorSlot) {
        return switch (armorSlot) {
            case 0 -> EquipmentSlot.FEET;
            case 1 -> EquipmentSlot.LEGS;
            case 2 -> EquipmentSlot.CHEST;
            case 3 -> EquipmentSlot.HEAD;
            default -> throw new IllegalArgumentException("Invalid armor slot: " + armorSlot);
        };
    }

    private int convertSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }

    private int findElytraSlot() {
        ClientPlayerEntity player = MC.player;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == Items.ELYTRA) {
                return convertSlot(i);
            }
        }
        return -1;
    }

    private Item findBestChestplateInInventory() {
        ClientPlayerEntity player = MC.player;
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
        ClientPlayerEntity player = MC.player;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == chestplate) {
                return convertSlot(i);
            }
        }
        return -1;
    }
}