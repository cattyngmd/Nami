package namidevelopment.kiriyaga.api.util;

import namidevelopment.kiriyaga.api.mixininterface.IClientPlayerInteractionManager;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

import static namidevelopment.kiriyaga.api.NamiApi.MC;

public class InventoryUtils {
    public static void attemptSwitch(int targetSlot) {
        if (MC.player == null || MC.level == null || MC.gameMode == null || targetSlot < 0 || targetSlot > 8)
            return;

        MC.player.getInventory().setSelectedSlot(targetSlot);
        syncSelectedSlot();
    }

    public static void syncSelectedSlot(){
        ((IClientPlayerInteractionManager) MC.gameMode).updateSlot(); // this one is the same as mc default one
    }

    public static int findHotbarItem(Predicate<ItemStack> predicate) {
        if (MC.player == null)
            return -1;

/*        if (predicate.test(MC.player.getInventory().getSelectedItem()))
            return MC.player.getInventory().getSelectedSlot();*/

        ItemStack selected = MC.player.getInventory().getSelectedItem();
        if (!selected.isEmpty() && predicate.test(selected))
            return MC.player.getInventory().getSelectedSlot();

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = MC.player.getInventory().getItem(slot);
            if (stack.isEmpty())
                continue;

            if (predicate.test(stack))
                return slot;
        }

        return -1;
    }
}
