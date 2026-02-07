package namidevelopment.kiriyaga.nami.impl.feature.combat;

import namidevelopment.kiriyaga.api.core.rotation.model.RotationRequest;
import namidevelopment.kiriyaga.api.event.EventPriority;
import namidevelopment.kiriyaga.api.annotation.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.feature.client.RotationsFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.api.model.setting.EnumSetting;
import namidevelopment.kiriyaga.api.model.setting.IntSetting;
import namidevelopment.kiriyaga.api.util.EnchantmentUtils;
import namidevelopment.kiriyaga.api.util.InventoryUtils;
import namidevelopment.kiriyaga.api.util.entity.TargetUtils;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;

import static namidevelopment.kiriyaga.api.NamiApi.INVENTORY_SERVICE;
import static namidevelopment.kiriyaga.api.NamiApi.ROTATION_SERVICE;
import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;import static namidevelopment.kiriyaga.api.util.PacketUtils.sendSequencedPacket;
import static namidevelopment.kiriyaga.api.util.entity.PlayerUtils.isPhased;

@RegisterFeature
public class AutoXPFeature extends Feature {

    public enum SwapMode {NORMAL, SILENT }

    public final IntSetting durability = addSetting(new IntSetting("Durability", 80, 70, 99));
    public final BoolSetting rotate = addSetting(new BoolSetting("Rotate", false));
    public final BoolSetting packet = addSetting(new BoolSetting("Packet", false));
    public final IntSetting packetShift = addSetting(new IntSetting("ShiftTicks", 2, 1, 6));
    public final BoolSetting whenNoTarget = addSetting(new BoolSetting("NoTarget", false));
    public final BoolSetting onlyPhased = addSetting(new BoolSetting("OnlyPhased", false));
    public final BoolSetting selfToggle = addSetting(new BoolSetting("SelfToggle", true));
    public final EnumSetting<SwapMode> swapMode = addSetting(new EnumSetting<>("Swap", SwapMode.SILENT));
    public final BoolSetting is1_12 = addSetting(new BoolSetting("1.12", false));

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
                InventoryUtils.attemptSwitch(xpSlot);
                MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);

                if (packet.get()) {
                    for (int l = 0; l < packetShift.get(); l++) {
                      sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, ROTATION_SERVICE.getStateHandler().getServerYaw(), ROTATION_SERVICE.getStateHandler().getServerPitch()));
                        }
                }

            }
            case SILENT -> {
                InventoryUtils.attemptSwitch(xpSlot);
                MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);

                if (packet.get()) {
                    for (int l = 0; l < packetShift.get(); l++) {
                        sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, ROTATION_SERVICE.getStateHandler().getServerYaw(), ROTATION_SERVICE.getStateHandler().getServerPitch()));
                    }
                }

                InventoryUtils.attemptSwitch(prevSlot);
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
