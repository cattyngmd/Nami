package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import static me.kiriyaga.nami.Nami.INVENTORY_MANAGER;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class AutoFireworkModule extends Module {

    public final DoubleSetting delaySeconds = addSetting(new DoubleSetting("delay", 4.5, 0.1, 25.0));
    public final IntSetting onLevel = addSetting(new IntSetting("on level", -64, -64, 360));

    private int tickDelay;
    private int lastUseTick = 0;

    public AutoFireworkModule() {
        super("auto firework", "Automatically fires fireworks.", ModuleCategory.of("world"), "autofirework");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (MC.player != null) {
            lastUseTick = MC.player.age - (int) Math.round(delaySeconds.get() * 20);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onTick(PreTickEvent ev) {
        if (!isEnabled()) return;
        if (MC.world == null || MC.player == null) return;

        if (MC.player.age < lastUseTick) {
            lastUseTick = MC.player.age - tickDelay;
        }

        tickDelay = (int) Math.round(delaySeconds.get() * 20);

        if (!MC.player.isGliding()) return;

        if (MC.player.getY() <= onLevel.get()) return;

        if (MC.player.age - lastUseTick < tickDelay) return;

        if (useItemAnywhere(Items.FIREWORK_ROCKET)) {
            lastUseTick = MC.player.age;
        }
    }

    private boolean useItemAnywhere(Item item) {
        int hotbarSlot = getSlotInHotbar(item);

        if (hotbarSlot != -1) {
            int prevSlot = MC.player.getInventory().getSelectedSlot();
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(hotbarSlot);
            MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(prevSlot);
            return true;
        }

        int invSlot = getSlotInInventory(item);
        if (invSlot != -1) {
            int selectedHotbarIndex = MC.player.getInventory().getSelectedSlot();
            int containerInvSlot = convertSlot(invSlot);

            INVENTORY_MANAGER.getClickHandler().swapSlot(containerInvSlot, selectedHotbarIndex);
            MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
            INVENTORY_MANAGER.getClickHandler().swapSlot(containerInvSlot, selectedHotbarIndex);
            return true;
        }

        return false;
    }

    private int getSlotInHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    private int getSlotInInventory(Item item) {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    private int convertSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }
}
