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
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Map;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@RegisterModule(category = "world")
public class AutoElytraModule extends Module {

    private boolean wasOnGroundLastTick = true;
    private static final int ARMOR_CHEST_SLOT = 6;
    private static final Map<Item, Integer> CHESTPLATE_PRIORITY = Map.of(
            Items.LEATHER_CHESTPLATE, 1,
            Items.GOLDEN_CHESTPLATE, 2,
            Items.CHAINMAIL_CHESTPLATE, 3,
            Items.IRON_CHESTPLATE, 4,
            Items.DIAMOND_CHESTPLATE, 5,
            Items.NETHERITE_CHESTPLATE, 6
    );

    private final BoolSetting fastSwap = addSetting(new BoolSetting("fast swap", false));
    private final IntSetting swapSlotSetting = addSetting(new IntSetting("swap slot", 8, 1, 9));
    private final BoolSetting pauseEfly = addSetting(new BoolSetting("pause on efly", true));

    private enum ElytraState {
        CHESTPLATE,
        ELYTRA
    }

    private ElytraState currentState = ElytraState.CHESTPLATE;
    private boolean swapRequested = false;
    private boolean needJumpImitate = false;
    private int jumpToggleStep = 0;

    public AutoElytraModule() {
        super("auto elytra", "Controlls elytra fly automatically, based QOL.", ModuleCategory.of("world"), "autoelytra");
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (MC.world == null || MC.player == null || (pauseEfly.get() && MODULE_MANAGER.getStorage().getByClass(ElytraFlyModule.class).isEnabled())) return;

        if (event.key == MC.options.jumpKey.getDefaultKey().getCode() && event.action == 1) {
            ClientPlayerEntity player = MC.player;
            boolean onGround = player.isOnGround();

            if (!onGround && !wasOnGroundLastTick) {
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
        if (player.isOnGround()) {
            ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);
            Item bestChestplate = getBestChestplate();

            if (bestChestplate != null &&
                    (chestItem.isEmpty() || chestItem.getItem() == Items.ELYTRA)) {

                int chestSlot = findChestplateSlot(bestChestplate);
                if (chestSlot != -1) {
                    int syncId = player.currentScreenHandler.syncId;
                    if (fastSwap.get() && (MC.currentScreen == null || MC.currentScreen instanceof InventoryScreen)) {
                        fastSwapItem(chestSlot, syncId, bestChestplate);
                    } else if (MC.currentScreen == null || MC.currentScreen instanceof InventoryScreen) {
                        swapWithArmor(chestSlot, syncId);
                    }
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

        int syncId = player.currentScreenHandler.syncId;

        if (currentState == ElytraState.CHESTPLATE) {
            int elytraSlot = findElytraSlot();
            if (elytraSlot != -1) {
                if (fastSwap.get()) {
                    fastSwapItem(elytraSlot, syncId, Items.ELYTRA);
                } else if (MC.currentScreen == null || MC.currentScreen instanceof InventoryScreen) {
                    swapWithArmor(elytraSlot, syncId);
                }
                currentState = ElytraState.ELYTRA;

                needJumpImitate = true;
                jumpToggleStep = 0;
            }
        } else {
            Item best = getBestChestplate();
            int chestSlot = findChestplateSlot(best);
            if (chestSlot != -1) {
                if (fastSwap.get()) {
                    fastSwapItem(chestSlot, syncId, best);
                } else if (MC.currentScreen == null || MC.currentScreen instanceof InventoryScreen) {
                    swapWithArmor(chestSlot, syncId);
                }
                currentState = ElytraState.CHESTPLATE;
            }
        }
    }

    private void handleJumpToggleSteps() {
        switch (jumpToggleStep) {
            case 0:
                MC.options.jumpKey.setPressed(false);
                jumpToggleStep++;
                break;
            case 1:
                MC.options.jumpKey.setPressed(true);
                jumpToggleStep++;
                break;
            case 2:
                MC.options.jumpKey.setPressed(false);
                jumpToggleStep = 0;
                needJumpImitate = false;
                break;
        }
    }

    private void swapWithArmor(int slot, int syncId) {
        ClientPlayerEntity player = MC.player;
        MC.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, player);
        MC.interactionManager.clickSlot(syncId, ARMOR_CHEST_SLOT, 0, SlotActionType.PICKUP, player);
        MC.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, player);
    }

    private void fastSwapItem(int inventorySlot, int syncId, Item itemToEquip) {
        ClientPlayerEntity player = MC.player;
        int hotbarSlot = swapSlotSetting.get() - 1;

        ItemStack hotbarStack = player.getInventory().getStack(hotbarSlot);
        if (hotbarStack.isEmpty() || hotbarStack.getItem() != itemToEquip) {
            return;
        }

        MC.interactionManager.clickSlot(syncId, ARMOR_CHEST_SLOT, hotbarSlot, SlotActionType.SWAP, player);
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
