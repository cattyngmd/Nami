package me.kiriyaga.nami.util.entity;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Arrays;

public class PlayerUtils {
    static boolean isPlayerWearingGold(ClientPlayerEntity player) {
        return Arrays.stream(EquipmentSlot.values())
                .filter(EquipmentSlot::isArmorSlot)
                .map(player::getEquippedStack)
                .anyMatch(PlayerUtils::isGoldArmor);
    }

    private static boolean isGoldArmor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == Items.GOLDEN_HELMET ||
                item == Items.GOLDEN_CHESTPLATE ||
                item == Items.GOLDEN_LEGGINGS ||
                item == Items.GOLDEN_BOOTS;
    }

    public static boolean isBroken(ItemStack stack, int threshold) {
        if (!stack.isDamageable()) return false;
        int max = stack.getMaxDamage();
        int damage = stack.getDamage();
        int percentRemaining = (int) (((max - damage) / (float) max) * 100);
        return percentRemaining <= threshold;
    }
}
