package namidevelopment.kiriyaga.api.util;

import namidevelopment.kiriyaga.api.mixininterface.IClientPlayerInteractionManager;

import static namidevelopment.kiriyaga.api.NamiApi.API_MC;
import static namidevelopment.kiriyaga.api.NamiApi.FEATURE_SERVICE;

public class InventoryUtils {
    public static void attemptSwitch(int targetSlot) {
        if (API_MC.player == null || API_MC.level == null || API_MC.gameMode == null || targetSlot < 0 || targetSlot > 8)
            return;

        API_MC.player.getInventory().setSelectedSlot(targetSlot);
        syncSelectedSlot();
    }

    public static void syncSelectedSlot(){
        ((IClientPlayerInteractionManager) API_MC.gameMode).updateSlot(); // this one is the same as mc default one
    }
}
