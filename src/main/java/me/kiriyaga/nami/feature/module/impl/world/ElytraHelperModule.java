package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.KeyInputEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.movement.ElytraFlyModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;

import java.util.Map;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class ElytraHelperModule extends Module {

    private boolean wasOnGroundLastTick = true;
    private static final int ARMOR_CHEST_SLOT = 2;
    private static final Map<Item, Integer> CHESTPLATE_PRIORITY = Map.of(
            Items.LEATHER_CHESTPLATE, 1,
            Items.GOLDEN_CHESTPLATE, 2,
            Items.CHAINMAIL_CHESTPLATE, 3,
            Items.IRON_CHESTPLATE, 4,
            Items.DIAMOND_CHESTPLATE, 5,
            Items.NETHERITE_CHESTPLATE, 6
    );

    private final BoolSetting pauseEfly = addSetting(new BoolSetting("pause on efly", true));

    private enum ElytraState {
        CHESTPLATE,
        ELYTRA
    }

    private ElytraState currentState = ElytraState.CHESTPLATE;
    private boolean swapRequested = false;
    private boolean needJumpImitate = false;
    private int jumpToggleStep = 0;

    public ElytraHelperModule() {
        super("elytra helper", "Controlls elytra fly automatically, based QOL.", ModuleCategory.of("world"), "autoelytra");
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (MC.world == null || MC.player == null || (pauseEfly.get() && MODULE_MANAGER.getStorage().getByClass(ElytraFlyModule.class).isEnabled())) return;

        if (event.key == MC.options.jumpKey.getDefaultKey().getCode() && event.action == 1) {
            ClientPlayerEntity player = MC.player;
            if (!player.isOnGround() && !wasOnGroundLastTick) {
                swapRequested = true;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPostTick(PostTickEvent event) {
        if (MC.world == null || MC.player == null) return;

        ClientPlayerEntity player = MC.player;
        boolean onGround = player.isOnGround();

        wasOnGroundLastTick = onGround;

        if (onGround) {
            ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);
            Item bestChestplate = getBestChestplate();

            if (bestChestplate != null &&
                    (chestItem.isEmpty() || chestItem.getItem() == Items.ELYTRA)) {

                int chestSlot = findChestplateSlot(bestChestplate);
                if (chestSlot != -1 && (MC.currentScreen == null || MC.currentScreen instanceof InventoryScreen)) {
                    swapArmor(ARMOR_CHEST_SLOT, chestSlot);
                    currentState = ElytraState.CHESTPLATE;
                }
            }
            return;
        }

        if (needJumpImitate) {
            handleJumpToggleSteps();
            return;
        }

        if (!swapRequested) return;
        swapRequested = false;

        if (currentState == ElytraState.CHESTPLATE) {
            int elytraSlot = findElytraSlot();
            if (elytraSlot != -1 && (MC.currentScreen == null || MC.currentScreen instanceof InventoryScreen)) {
                swapArmor(ARMOR_CHEST_SLOT, elytraSlot);
                currentState = ElytraState.ELYTRA;
                needJumpImitate = true;
                jumpToggleStep = 0;
            }
        } else {
            Item best = getBestChestplate();
            int chestSlot = findChestplateSlot(best);
            if (chestSlot != -1 && (MC.currentScreen == null || MC.currentScreen instanceof InventoryScreen)) {
                swapArmor(ARMOR_CHEST_SLOT, chestSlot);
                currentState = ElytraState.CHESTPLATE;
            }
        }
    }

    private void handleJumpToggleSteps() {
        switch (jumpToggleStep) {
            case 0 -> {
                MC.options.jumpKey.setPressed(false);
                jumpToggleStep++;
            }
            case 1 -> {
                MC.options.jumpKey.setPressed(true);
                jumpToggleStep++;
            }
            case 2 -> {
                MC.options.jumpKey.setPressed(false);
                jumpToggleStep = 0;
                needJumpImitate = false;
            }
        }
    }

    private void swapArmor(int armorSlot, int slot) {
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

    private Item getBestChestplate() {
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

    private int convertSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }
}