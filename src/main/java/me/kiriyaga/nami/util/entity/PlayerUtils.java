package me.kiriyaga.nami.util.entity;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

import java.util.Arrays;

import static me.kiriyaga.nami.Nami.MC;

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

    public static boolean isPhased(Entity e) {
        if (e == null || MC.world == null) return false;

        Box box = e.getBoundingBox();
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.ceil(box.maxY);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    VoxelShape shape = MC.world.getBlockState(pos).getCollisionShape(MC.world, pos);
                    if (!shape.isEmpty() && shape.getBoundingBox().offset(pos).intersects(box)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
