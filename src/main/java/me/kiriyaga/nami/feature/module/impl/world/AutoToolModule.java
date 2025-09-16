package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.core.executable.model.ExecutableEventType;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.BreakBlockEvent;
import me.kiriyaga.nami.event.impl.StartBreakingBlockEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.EnchantmentUtils;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class AutoToolModule extends Module {

    private final IntSetting damageThreshold = addSetting(new IntSetting("durability", 3, 0, 15));

    public AutoToolModule() {
        super("auto tool", "Auto selects the currently best mining tool from your hotbar.", ModuleCategory.of("world"), "autotool");
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void onBlockAttack(StartBreakingBlockEvent event) {
        if (MC.player == null || MC.world == null || MC.player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        EXECUTABLE_MANAGER.getRequestHandler().submit(() -> { // we are not on main thread!
            BlockPos targetPos = event.blockPos;
            BlockState targetState = MC.world.getBlockState(targetPos);

            int bestSlot = -1;
            float bestSpeed = 1.0f;

            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = MC.player.getInventory().getStack(slot);
                if (stack.isEmpty())
                    continue;

                if (isBroken(stack))
                    continue;

                float efficiencyLevel = EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);
                float miningSpeed = stack.getMiningSpeedMultiplier(targetState);
                float totalSpeed = efficiencyLevel + miningSpeed;

                if (totalSpeed > bestSpeed) {
                    bestSpeed = totalSpeed;
                    bestSlot = slot;
                }
            }

            if (bestSlot != -1)
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(bestSlot);
            }, 0, ExecutableEventType.PRE_TICK);
    }

    private boolean isBroken(ItemStack stack) {
        if (!stack.isDamageable()) return false;
        int max = stack.getMaxDamage();
        int damage = stack.getDamage();
        int percentRemaining = (int) (((max - damage) / (float) max) * 100);
        return percentRemaining <= damageThreshold.get();
    }
}
