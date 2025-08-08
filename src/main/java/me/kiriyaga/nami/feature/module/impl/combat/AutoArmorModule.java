package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.gui.screen.ClickGuiScreen;
import me.kiriyaga.nami.feature.gui.screen.HudEditorScreen;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import me.kiriyaga.nami.util.EnchantmentUtils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;

import java.util.*;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class AutoArmorModule extends Module {

    private enum ProtectionPriority {PROT, BLAST }
    private enum BootsPriority { LEATHER, GOLDEN, BEST }
    private enum HelmetPriority { BEST, TURTLE, GOLDEN, PUMPKIN, NONE }

    private final EnumSetting<ProtectionPriority> protectionPriority = addSetting(new EnumSetting<>("protection", ProtectionPriority.PROT));
    private final IntSetting damageThreshold = addSetting(new IntSetting("damage", 3, 1, 15));
    private final EnumSetting<HelmetPriority> helmetSetting = addSetting(new EnumSetting<>("helmet", HelmetPriority.BEST));
    private final BoolSetting helmetSafety = addSetting(new BoolSetting("helmet safety", false));
    private final EnumSetting<BootsPriority> bootsPriority = addSetting(new EnumSetting<>("boots", BootsPriority.BEST));
    private final BoolSetting elytraPriority = addSetting(new BoolSetting("elytra priority", false));

    private static final Set<Item> ARMOR_ITEMS_HEAD = Set.of(Items.LEATHER_HELMET, Items.GOLDEN_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET, Items.TURTLE_HELMET);
    private static final Set<Item> ARMOR_ITEMS_CHEST = Set.of(Items.LEATHER_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE, Items.ELYTRA);
    private static final Set<Item> ARMOR_ITEMS_LEGS = Set.of(Items.LEATHER_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
    private static final Set<Item> ARMOR_ITEMS_FEET = Set.of(Items.LEATHER_BOOTS, Items.GOLDEN_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);

    public AutoArmorModule() {
        super("auto armor", "Automatically equips best armor.", ModuleCategory.of("combat"), "autoarmor","фгещфкьщк");
        helmetSafety.setShowCondition(() -> helmetSetting.get() == HelmetPriority.NONE);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onTick(PostTickEvent event) {
        if (MC.world == null || MC.player == null) return;
        if (MC.currentScreen == null || MC.currentScreen instanceof InventoryScreen || MC.currentScreen instanceof ChatScreen || MC.currentScreen instanceof ClickGuiScreen || MC.currentScreen instanceof HudEditorScreen) {
            Entity target = ENTITY_MANAGER.getTarget();

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!isArmorSlot(slot)) continue;

                ItemStack current = MC.player.getEquippedStack(slot);
                ItemStack best = null;

                if (slot == EquipmentSlot.HEAD) {
                    if (helmetSetting.get() == HelmetPriority.NONE) {
                        if (helmetSafety.get() && target != null) {
                            best = findBestHelmet(current, true);
                        } else {
                            best = ItemStack.EMPTY;
                        }
                    } else {
                        best = findBestHelmet(current, false);
                    }
                } else if (slot == EquipmentSlot.FEET) {
                    best = findBestBoots(current);
                } else {
                    best = findBestArmor(slot, current);
                }

                if (best != null) {
                    if (best.isEmpty()) {
                        if (!current.isEmpty()) {
                            int invSlot = findEmptySlot();
                            if (invSlot != -1) {
                                swap(slot, invSlot);
                            }
                        }
                    } else if (!ItemStack.areEqual(best, current)) {
                        int invSlot = findInventorySlot(best);
                        if (invSlot != -1) {
                            swap(slot, invSlot);
                        }
                    }
                }
            }
        }
    }

    private ItemStack findBestHelmet(ItemStack current, boolean forceBest) {
        ClientPlayerEntity player = MC.player;
        List<ItemStack> candidates = new ArrayList<>();

        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();

            switch (helmetSetting.get()) {
                case TURTLE:
                    if (item == Items.TURTLE_HELMET) return stack;
                    break;
                case GOLDEN:
                    if (item == Items.GOLDEN_HELMET) return stack;
                    break;
                case PUMPKIN:
                    if (item == Items.CARVED_PUMPKIN) return stack;
                    break;
                case BEST:
                    if (isArmorForSlot(stack, EquipmentSlot.HEAD)) candidates.add(stack);
                    break;
                case NONE:
                    if (forceBest && isArmorForSlot(stack, EquipmentSlot.HEAD)) candidates.add(stack);
                    break;
            }
        }

        if (candidates.isEmpty()) return forceBest ? null : null;

        ItemStack best = current;
        for (ItemStack candidate : candidates) {
            if (isBetterArmor(candidate, best)) best = candidate;
        }

        return best != current ? best : null;
    }

    private ItemStack findBestBoots(ItemStack current) {
        ClientPlayerEntity player = MC.player;
        List<ItemStack> candidates = new ArrayList<>();

        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();
            if (!isArmorForSlot(stack, EquipmentSlot.FEET)) continue;

            switch (bootsPriority.get()) {
                case LEATHER:
                    if (item == Items.LEATHER_BOOTS) return stack;
                    break;
                case GOLDEN:
                    if (item == Items.GOLDEN_BOOTS) return stack;
                    break;
                case BEST:
                    candidates.add(stack);
                    break;
            }
        }

        if (candidates.isEmpty()) return null;

        ItemStack best = current;
        for (ItemStack candidate : candidates) {
            if (isBetterArmor(candidate, best)) best = candidate;
        }

        return best != current ? best : null;
    }

    private boolean isBetterArmor(ItemStack a, ItemStack b) {
        if (a.isEmpty()) return false;
        if (b.isEmpty()) return true;

        int aMat = getMaterialScore(a.getItem());
        int bMat = getMaterialScore(b.getItem());
        if (aMat != bMat) return aMat > bMat;

        int aProt = EnchantmentUtils.getEnchantmentLevel(a, Enchantments.PROTECTION);
        int bProt = EnchantmentUtils.getEnchantmentLevel(b, Enchantments.PROTECTION);
        int aBlast = EnchantmentUtils.getEnchantmentLevel(a, Enchantments.BLAST_PROTECTION);
        int bBlast = EnchantmentUtils.getEnchantmentLevel(b, Enchantments.BLAST_PROTECTION);

        if (protectionPriority.get() == ProtectionPriority.BLAST) {
            if (aBlast != bBlast) return aBlast > bBlast;
        } else {
            if (aProt != bProt) return aProt > bProt;
        }

        return (aProt + aBlast) > (bProt + bBlast);
    }

    private int findEmptySlot() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.isEmpty()) return i;
        }
        return -1;
    }

    private boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }

    private void swap(EquipmentSlot armorSlot, int slot) {
        ItemStack equipped = MC.player.getEquippedStack(armorSlot);
        int realSlot = slot < 9 ? slot + 36 : slot;
        int armorSlotIndex = switch (armorSlot) {
            case HEAD -> 5;
            case CHEST -> 6;
            case LEGS -> 7;
            case FEET -> 8;
            default -> throw new IllegalArgumentException();
        };

        INVENTORY_MANAGER.getClickHandler().pickupSlot(realSlot);
        boolean hasEquipped = !equipped.isEmpty();
        INVENTORY_MANAGER.getClickHandler().pickupSlot(armorSlotIndex);
        if (hasEquipped) {
            INVENTORY_MANAGER.getClickHandler().pickupSlot(realSlot);
        }
    }

    private ItemStack findBestArmor(EquipmentSlot targetSlot, ItemStack current) {
        ClientPlayerEntity player = MC.player;
        List<ItemStack> candidates = new ArrayList<>();

        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            if (isBroken(stack)) continue;

            if (!isArmorForSlot(stack, targetSlot)) continue;

            if (targetSlot == EquipmentSlot.CHEST && stack.getItem() == Items.ELYTRA && elytraPriority.get()) {
                if (current.isEmpty() || isBroken(current) || current.getItem() != Items.ELYTRA) {
                    return stack;
                } else {
                    continue; // rekursia
                }
            }

            if (targetSlot == EquipmentSlot.HEAD) {
                switch (helmetSetting.get()) {
                    case TURTLE:
                        if (stack.getItem() == Items.TURTLE_HELMET) return stack;
                        break;
                    case GOLDEN:
                        if (stack.getItem() == Items.GOLDEN_HELMET) return stack;
                        break;
                    case BEST:
                        break;
                }
                if (helmetSetting.get() != HelmetPriority.BEST) continue;
            }

            candidates.add(stack);
        }

        if (targetSlot == EquipmentSlot.CHEST && current.getItem() == Items.ELYTRA && elytraPriority.get() && !isBroken(current)) {
            return null;
        }

        if (candidates.isEmpty()) return null;

        ItemStack best = current;

        for (ItemStack candidate : candidates) {
            if (isBetterArmor(candidate, best)) {
                best = candidate;
            }
        }

        return best != current ? best : null;
    }

    // yes this is shitcode, no, i dont see any other solution, no more armormaterial etc, only components
    private int getMaterialScore(Item item) {
        if (item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE || item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS)
            return 6;
        if (item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE || item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS)
            return 5;
        if (item == Items.IRON_HELMET || item == Items.IRON_CHESTPLATE || item == Items.IRON_LEGGINGS || item == Items.IRON_BOOTS)
            return 4;
        if (item == Items.CHAINMAIL_HELMET || item == Items.CHAINMAIL_CHESTPLATE || item == Items.CHAINMAIL_LEGGINGS || item == Items.CHAINMAIL_BOOTS)
            return 3;
        if (item == Items.GOLDEN_HELMET || item == Items.GOLDEN_CHESTPLATE || item == Items.GOLDEN_LEGGINGS || item == Items.GOLDEN_BOOTS)
            return 2;
        if (item == Items.LEATHER_HELMET || item == Items.LEATHER_CHESTPLATE || item == Items.LEATHER_LEGGINGS || item == Items.LEATHER_BOOTS)
            return 1;
        return 0;
    }

    private boolean isArmorForSlot(ItemStack stack, EquipmentSlot slot) {
        Item item = stack.getItem();
        switch (slot) {
            case HEAD:
                return ARMOR_ITEMS_HEAD.contains(item);
            case CHEST:
                return ARMOR_ITEMS_CHEST.contains(item);
            case LEGS:
                return ARMOR_ITEMS_LEGS.contains(item);
            case FEET:
                return ARMOR_ITEMS_FEET.contains(item);
            default:
                return false;
        }
    }

    private int findInventorySlot(ItemStack target) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (ItemStack.areEqual(stack, target)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isBroken(ItemStack stack) {
        if (!stack.isDamageable()) return false;
        int max = stack.getMaxDamage();
        int damage = stack.getDamage();
        int percent = (int) ((damage / (float) max) * 100);
        return percent >= damageThreshold.get();
    }
}
