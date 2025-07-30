package me.kiriyaga.nami.feature.module.impl.combat;

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

    public ClickPearlModule() {
        super("click pearl", "Throws a pearl on activation.", ModuleCategory.of("combat"), "clickpearl", "сдшслашкуцщкл");
    }

    @Override
    public void onEnable(){
        toggle(); // as-is so we dont care
        if (MC.world == null || MC.player == null)
            return;

        if (MC.player.isGliding() && glideFirework.get()){
            int i = getSlot(Items.FIREWORK_ROCKET);
            if (i == -1)
                return;

            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(i);
            MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
            INVENTORY_MANAGER.getSyncHandler().swapSync();
            return;
        }

        int i = getSlot(Items.ENDER_PEARL);
        if (i == -1 || MC.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL.getDefaultStack()))
            return;

        //INVENTORY_MANAGER.getSlotHandler().sendSlotPacket(i);
        INVENTORY_MANAGER.getSlotHandler().attemptSwitch(i);
        MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
        INVENTORY_MANAGER.getSyncHandler().swapSync();
    }

    private int getSlot(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }
}
