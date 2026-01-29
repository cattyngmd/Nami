package me.kiriyaga.nami.impl.feature.impl.combat;

import me.kiriyaga.nami.api.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.feature.impl.client.RotationsFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.EnumSetting;
import me.kiriyaga.nami.impl.setting.impl.IntSetting;
import me.kiriyaga.nami.util.EnchantmentUtils;
import me.kiriyaga.nami.util.entity.TargetUtils;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.PacketUtils.sendSequencedPacket;
import static me.kiriyaga.nami.util.entity.PlayerUtils.isPhased;

@RegisterFeature
public class AutoXPFeature extends Feature {

    public enum SwapMode {NORMAL, SILENT }

    private final IntSetting durability = addSetting(new IntSetting("Durability", 80, 70, 99));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", false));
    private final BoolSetting packet = addSetting(new BoolSetting("Packet", false));
    private final IntSetting packetShift = addSetting(new IntSetting("ShiftTicks", 3, 1, 6));
    private final BoolSetting whenNoTarget = addSetting(new BoolSetting("NoTarget", false));
    private final BoolSetting onlyPhased = addSetting(new BoolSetting("OnlyPhased", true));
    private final BoolSetting selfToggle = addSetting(new BoolSetting("SelfToggle", true));
    private final EnumSetting<SwapMode> swapMode = addSetting(new EnumSetting<>("Swap", SwapMode.NORMAL));
    private final BoolSetting is1_12 = addSetting(new BoolSetting("1.12", false));

    public AutoXPFeature() {
        super("AutoXP", "Automatically repair armor with XP bottles.", FeatureCategory.of("Combat"), "autoxp");
        packetShift.setShowCondition(packet::get);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onPreTickEvent(PreTickEvent ev) {
        if (!isEnabled() || MC.player == null || MC.level == null) return;

        if (is1_12.get() && anyAboveThreshold()) { // old minecraft versions does not have mending bugfix
            if (selfToggle.get())
                toggle();
            return;
        }

        if (whenNoTarget.get() && TargetUtils.getTarget() != null) {
            if (selfToggle.get())
                toggle();
            return;
        }

        if (onlyPhased.get() && !isPhased(MC.player)) {
            if (selfToggle.get())
                toggle();

            return;
        }

        int xpSlot = getSlotInHotbar(Items.EXPERIENCE_BOTTLE);
        if (xpSlot == -1) {
            if (selfToggle.get())
                toggle();
            return;
        }

        if (!shouldRepair()) {
            if (selfToggle.get())
                toggle();
            return;
        }

        if (rotate.get()) {

            ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(
                            this.name,
                            6,
                            MC.player.getYRot(),
                            90.0f,
                            RotationsFeature.RotationMode.MOTION // only motion here sorry
                    )
            );

            if (!ROTATION_SERVICE.getRequestHandler().isCompleted(this.name)) return;
        }

        int prevSlot = MC.player.getInventory().getSelectedSlot();

        switch (swapMode.get()) {
            case NORMAL -> {
                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(xpSlot);
                MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);

                if (packet.get()) {
                    for (int l = 0; l < packetShift.get(); l++) {
                      sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, ROTATION_SERVICE.getStateHandler().getServerYaw(), ROTATION_SERVICE.getStateHandler().getServerPitch()));
                        }
                }

            }
            case SILENT -> {
                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(xpSlot);
                MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);

                if (packet.get()) {
                    for (int l = 0; l < packetShift.get(); l++) {
                        sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, ROTATION_SERVICE.getStateHandler().getServerYaw(), ROTATION_SERVICE.getStateHandler().getServerPitch()));
                    }
                }

                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(prevSlot);
            }
        }
    }

    private boolean shouldRepair() {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = MC.player.getItemBySlot(slot);
            if (!hasMending(stack))
                continue;

            if (isBelow(stack)) return true;
        }
        return false;
    }

    private boolean anyAboveThreshold() {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = MC.player.getItemBySlot(slot);
            if (isAbove(stack)) return true;
        }
        return false;
    }


    private boolean isBelow(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.isDamageableItem()) return false;
        int max = stack.getMaxDamage();
        int damage = stack.getDamageValue();
        int percentRemaining = (int) (((max - damage) / (float) max) * 100);
        return percentRemaining <= durability.get();
    }

    private boolean isAbove(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.isDamageableItem()) return false;
        int max = stack.getMaxDamage();
        int damage = stack.getDamageValue();
        int percentRemaining = (int) (((max - damage) / (float) max) * 100);
        return percentRemaining > durability.get();
    }

    private int getSlotInHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) return i;
        }
        return -1;
    }

    private boolean hasMending(ItemStack stack) {
        return EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.MENDING) > 0;
    }
}
