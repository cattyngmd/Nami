package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;


import static me.kiriyaga.nami.Nami.INVENTORY_MANAGER;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class ClickPearlModule extends Module {

    public final BoolSetting glideFirework = addSetting(new BoolSetting("glide firework", true));
    private boolean hasActivated = false;

    public ClickPearlModule() {
        super("click pearl", "Throws a pearl on activation.", ModuleCategory.of("combat"), "clickpearl", "сдшслашкуцщкл");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onTick(PreTickEvent ev) { // onTick due to post-held item change
        if (!isEnabled() || hasActivated)
            return;

        if (MC.world == null || MC.player == null)
            return;

        hasActivated = true;
        toggle();

        if (MC.player.isGliding() && glideFirework.get()) {
            int slot = getSlot(Items.FIREWORK_ROCKET);
            int prevSlot = MC.player.getInventory().getSelectedSlot();
            if (slot == -1)
                return;

            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(slot);
            MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(prevSlot);
            return;
        }

        int slot = getSlot(Items.ENDER_PEARL);
        if (slot == -1 || MC.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL.getDefaultStack()))
            return;

        int prevSlot = MC.player.getInventory().getSelectedSlot();

        INVENTORY_MANAGER.getSlotHandler().attemptSwitch(slot);
        MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
        INVENTORY_MANAGER.getSlotHandler().attemptSwitch(prevSlot);
    }

    @Override
    public void onEnable() {
        hasActivated = false;
    }

    private int getSlot(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }
}