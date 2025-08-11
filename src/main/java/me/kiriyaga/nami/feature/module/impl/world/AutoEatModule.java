package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.Event;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PlaceBlockEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
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
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicBoolean;

import static me.kiriyaga.nami.Nami.INVENTORY_MANAGER;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class AutoEatModule extends Module {

    private final IntSetting swapDelayTicksSetting = addSetting(new IntSetting("delay", 5, 1, 20));
    private final DoubleSetting minHunger = addSetting(new DoubleSetting("hunger", 19.0, 0.0, 19.0));
    private final DoubleSetting minHealth = addSetting(new DoubleSetting("health", 0.0, 0.0, 19.0));
    private final BoolSetting allowGapples = addSetting(new BoolSetting("gapples", true));
    private final BoolSetting allowPoisoned = addSetting(new BoolSetting("poisoned", false));

    private final AtomicBoolean eating = new AtomicBoolean(false);
    private volatile int swapCooldown = 0;

    public AutoEatModule() {
        super("auto eat", "Automatically eats best food.", ModuleCategory.of("world"), "фгещуфв", "autoeat");
    }

    @Override
    public void onDisable() {
        setUseHeld(false);
        eating.set(false);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null) return;

        if (!eating.get())
            setUseHeld(false);

        eating.set(false);

        double hunger = MC.player.getHungerManager().getFoodLevel();
        double health = MC.player.getHealth();
        boolean needsEat = hunger < minHunger.get() || health < minHealth.get();
        if (!needsEat) {
            eating.set(false);
            return;
        }

        int bestSlot = getBestFoodSlot();
        if (bestSlot == -1) {
            eating.set(false);
            return;
        }

        int currentSlot = MC.player.getInventory().getSelectedSlot();

        if (currentSlot == bestSlot) {
            setUseHeld(true);
            eating.set(true);
        } else {
            if (swapCooldown > 0) {
                swapCooldown--;
            } else {
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(bestSlot);
                swapCooldown = swapDelayTicksSetting.get();
            }
            eating.set(false);
        }
    }

//            @SubscribeEvent(priority = EventPriority.HIGHEST)
//        private void onPlaceBlock(PlaceBlockEvent event) {
//            if (MC.player != null && MC.world != null && eating.get())
//                event.cancel();
//        }

    private int getBestFoodSlot() {
        int bestSlot = -1;
        float bestScore = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
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
        float nutrition = food.comp_2491();
        float saturation = food.comp_2492();
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
        KeyBinding useKey = MC.options.useKey;
        InputUtil.Key boundKey = ((KeyBindingAccessor) useKey).getBoundKey();

        boolean physicallyPressed = false;

        if (boundKey.getCategory() == InputUtil.Type.KEYSYM) {
            int keyCode = boundKey.getCode();
            if (keyCode >= 0) {
                physicallyPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), keyCode);
            }
        } else if (boundKey.getCategory() == InputUtil.Type.MOUSE) {
            int mouseCode = boundKey.getCode();
            if (mouseCode >= 0) {
                physicallyPressed = GLFW.glfwGetMouseButton(MC.getWindow().getHandle(), mouseCode) == GLFW.GLFW_PRESS;
            }
        }

        useKey.setPressed(physicallyPressed || held);
    }
}
