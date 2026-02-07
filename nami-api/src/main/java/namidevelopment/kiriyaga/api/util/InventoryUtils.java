package namidevelopment.kiriyaga.api.util;

import namidevelopment.kiriyaga.api.mixininterface.IClientPlayerInteractionManager;

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
}
