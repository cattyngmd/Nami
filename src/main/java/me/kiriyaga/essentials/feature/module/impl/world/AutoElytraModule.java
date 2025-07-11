package me.kiriyaga.essentials.feature.module.impl.world;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PostTickEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Map;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class AutoElytraModule extends Module {

    private static final int ARMOR_CHEST_SLOT = 6;
    private boolean hasJumpPressedOnce = false;
    private boolean needJumpToggle = false;
    private boolean jumpKeyPrevState = false;

    private static final Map<Item, Integer> CHESTPLATE_PRIORITY = Map.of(
            Items.LEATHER_CHESTPLATE, 1,
            Items.GOLDEN_CHESTPLATE, 2,
            Items.CHAINMAIL_CHESTPLATE, 3,
            Items.IRON_CHESTPLATE, 4,
            Items.DIAMOND_CHESTPLATE, 5,
            Items.NETHERITE_CHESTPLATE, 6
    );

    private boolean lastJumping = false;

    private final BoolSetting fastSwap = addSetting(new BoolSetting("fast swap", false));
    private final IntSetting swapSlotSetting = addSetting(new IntSetting("swap slot", 8, 1, 9));

    public AutoElytraModule() {
        super("auto elytra", "Automatically swaps elytra and chestplate based on flight/jump state.", Category.world, "autoelytra");
    }

    private enum ElytraState {
        CHESTPLATE,
        ELYTRA
    }

    private ElytraState currentState = ElytraState.CHESTPLATE;

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPostTick(PostTickEvent event) {
        if (MINECRAFT.world == null || MINECRAFT.player == null) return;
        ClientPlayerEntity player = MINECRAFT.player;
        int syncId = player.currentScreenHandler.syncId;

        boolean jumpKeyPressed = MINECRAFT.options.jumpKey.isPressed();

        // --- Логика имитации нажатия пробела после свапа элитры ---
        if (needJumpToggle) {
            if (!jumpKeyPrevState) {
                // Если пробел НЕ зажат — нажимаем и сразу отпускаем
                MINECRAFT.options.jumpKey.setPressed(true);
                MINECRAFT.options.jumpKey.setPressed(false);
                needJumpToggle = false;
            } else {
                // Если пробел зажат — отжать -> нажать -> отжать по тикам
                handleJumpToggleSteps();
                return; // пропускаем остальной код в этом тике
            }
        }

        jumpKeyPrevState = jumpKeyPressed;

        ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);
        Item chestItemType = chestItem.getItem();

        boolean onGround = player.isOnGround();
        boolean jumping = jumpKeyPressed;

        if (onGround) {
            currentState = ElytraState.CHESTPLATE;
            hasJumpPressedOnce = false;

            Item bestChestplate = getBestChestplate();
            if (bestChestplate != null && chestItemType != bestChestplate) {
                int slot = findChestplateSlot(bestChestplate);
                if (slot != -1) {
                    if (fastSwap.get()) {
                        fastSwapItem(slot, syncId, bestChestplate);
                    } else if (MINECRAFT.currentScreen == null || MINECRAFT.currentScreen instanceof InventoryScreen) {
                        swapWithArmor(slot, syncId);
                    }
                }
            }
        } else {
            if (jumping && !lastJumping) {
                if (!hasJumpPressedOnce) {
                    hasJumpPressedOnce = true;
                } else {
                    if (currentState == ElytraState.CHESTPLATE) {
                        int elytraSlot = findElytraSlot();
                        if (elytraSlot != -1) {
                            if (fastSwap.get()) {
                                fastSwapItem(elytraSlot, syncId, Items.ELYTRA);
                            } else if (MINECRAFT.currentScreen == null || MINECRAFT.currentScreen instanceof InventoryScreen) {
                                swapWithArmor(elytraSlot, syncId);
                            }
                            currentState = ElytraState.ELYTRA;

                            // после свапа элитры ставим флаг имитации прыжка
                            needJumpToggle = true;
                        }
                    } else if (currentState == ElytraState.ELYTRA) {
                        Item best = getBestChestplate();
                        int slot = findChestplateSlot(best);
                        if (slot != -1) {
                            if (fastSwap.get()) {
                                fastSwapItem(slot, syncId, best);
                            } else if (MINECRAFT.currentScreen == null || MINECRAFT.currentScreen instanceof InventoryScreen) {
                                swapWithArmor(slot, syncId);
                            }
                            currentState = ElytraState.CHESTPLATE;
                        }
                    }
                    hasJumpPressedOnce = false;
                }
            }
        }

        lastJumping = jumping;
    }

    private void swapWithArmor(int slot, int syncId) {
        ClientPlayerEntity player = MINECRAFT.player;
        MINECRAFT.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, player);
        MINECRAFT.interactionManager.clickSlot(syncId, ARMOR_CHEST_SLOT, 0, SlotActionType.PICKUP, player);
        MINECRAFT.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, player);
    }

    private void fastSwapItem(int inventorySlot, int syncId, Item itemToEquip) {
        ClientPlayerEntity player = MINECRAFT.player;
        int hotbarSlot = swapSlotSetting.get() - 1;

        ItemStack hotbarStack = player.getInventory().getStack(hotbarSlot);
        if (hotbarStack.isEmpty() || hotbarStack.getItem() != itemToEquip) {
            return;
        }

        MINECRAFT.interactionManager.clickSlot(syncId, ARMOR_CHEST_SLOT, hotbarSlot, SlotActionType.SWAP, player);
    }

    private Item getBestChestplate() {
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

    private int convertSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }

    private int jumpToggleStep = 0;
    private void handleJumpToggleSteps() {
        switch (jumpToggleStep) {
            case 0:
                MINECRAFT.options.jumpKey.setPressed(false); // отжать
                jumpToggleStep++;
                break;
            case 1:
                MINECRAFT.options.jumpKey.setPressed(true); // нажать
                jumpToggleStep++;
                break;
            case 2:
                MINECRAFT.options.jumpKey.setPressed(false); // отжать
                jumpToggleStep = 0;
                needJumpToggle = false;
                break;
        }
    }
}
