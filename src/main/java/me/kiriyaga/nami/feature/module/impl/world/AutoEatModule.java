package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.mixin.ClientPlayerInteractionManagerAccessor;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.MINECRAFT;
public class AutoEatModule extends Module {

    private final IntSetting swapDelayTicksSetting = addSetting(new IntSetting("delay", 5, 1, 20));
    private final DoubleSetting minHunger = addSetting(new DoubleSetting("hunger", 19.0, 0.0, 19.0));
    private final DoubleSetting minHealth = addSetting(new DoubleSetting("health", 0.0, 0.0, 19.0));
    private final BoolSetting allowGapples = addSetting(new BoolSetting("gapples", true));
    private final BoolSetting allowPoisoned = addSetting(new BoolSetting("poisoned", false));

    private boolean eating = false;
    private int swapCooldown = 0;

    public AutoEatModule() {
        super("auto eat", "Automatically eats best food.", Category.world, "фгещуфв", "autoeat");
    }

    @Override
    public void onDisable() {
        setUseHeld(false);
        // Убрали возврат в предыдущий слот
    }

    @SubscribeEvent
    public void onPreTick(PreTickEvent event) {
        if (MINECRAFT.player == null) return;

        if (swapCooldown > 0) {
            swapCooldown--;
            return;
        }

        if (eating && !MINECRAFT.player.isUsingItem()) {
            setUseHeld(false);
            eating = false;
            swapCooldown = (int) swapDelayTicksSetting.get();
            return;
        }

        double hunger = MINECRAFT.player.getHungerManager().getFoodLevel();
        double health = MINECRAFT.player.getHealth();

        if (hunger >= minHunger.get() && health >= minHealth.get()) {
            if (eating) {
                setUseHeld(false);
                eating = false;
                swapCooldown = (int) swapDelayTicksSetting.get();
            }
            return;
        }

        int bestSlot = getBestFoodSlot();
        if (bestSlot == -1) {
            if (eating) {
                setUseHeld(false);
                eating = false;
                swapCooldown = (int) swapDelayTicksSetting.get();
            }
            return;
        }

        int currentSlot = MINECRAFT.player.getInventory().getSelectedSlot();

        if (!eating) {
            if (currentSlot != bestSlot) {
                selectHotbarSlotImmediate(bestSlot);
                swapCooldown = (int) swapDelayTicksSetting.get();
            }
            setUseHeld(true);
            eating = true;
        }
    }

    private int getBestFoodSlot() {
        int bestSlot = -1;
        float bestScore = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = MINECRAFT.player.getInventory().getStack(i);
            float score = getFoodScore(stack);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    private float getFoodScore(ItemStack stack) {
        if (stack.isEmpty() || !stack.getItem().getComponents().contains(DataComponentTypes.FOOD)) {
            return -1;
        }

        Item item = stack.getItem();

        if (!allowPoisoned.get() && isPoisonedFood(item)) {
            return -1;
        }

        if (!allowGapples.get() && isGapple(item)) {
            return -1;
        }

        FoodComponent food = item.getComponents().get(DataComponentTypes.FOOD);
        float nutrition = food.nutrition();
        float saturation = food.saturation();
        float totalValue = nutrition + saturation;

        if (isGapple(item)) {
            return allowGapples.get() ? totalValue - 0.5f : -1;
        }

        if (isPoisonedFood(item)) {
            return allowPoisoned.get() ? totalValue - 1.5f : -1;
        }

        return totalValue;
    }

    private boolean isGapple(Item item) {
        return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE;
    }

    private boolean isPoisonedFood(Item item) {
        return item == Items.ROTTEN_FLESH
                || item == Items.PUFFERFISH
                || item == Items.SPIDER_EYE
                || item == Items.CHORUS_FRUIT;
    }

    private void setUseHeld(boolean held) {
        KeyBinding useKey = MINECRAFT.options.useKey;
        InputUtil.Key boundKey = ((KeyBindingAccessor) useKey).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MINECRAFT.getWindow().getHandle(), keyCode);
        useKey.setPressed(physicallyPressed || held);
    }

    public static boolean selectHotbarSlotImmediate(int slot) {
        if (slot == 45) return true;
        if (slot < 0 || slot > 8) return false;

        MINECRAFT.player.getInventory().setSelectedSlot(slot);
        ((ClientPlayerInteractionManagerAccessor) MINECRAFT.interactionManager).callSyncSelectedSlot();
        return true;
    }
}
