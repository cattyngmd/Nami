package namidevelopment.kiriyaga.api.api.inventory;

import static namidevelopment.kiriyaga.api.NamiApi.*;


public class InventorySlotHandler {

    // this shit was overengeneered and uncompat with like any client so i made it mc vanilla way
    public void attemptSwitch(int targetSlot) {
        if (API_MC.player == null || API_MC.level == null || API_MC.gameMode == null || targetSlot < 0 || targetSlot > 8)
            return;

        API_MC.player.getInventory().setSelectedSlot(targetSlot);
        syncSelectedSlot();
    }

    public void syncSelectedSlot(){
        ((IClientPlayerInteractionManager) API_MC.gameMode).updateSlot(); // this one is the same as mc default one

        if (FEATURE_SERVICE.getStorage().getByClass(PatchFeature.class).silentSwapFix.get())
            FEATURE_SERVICE.getStorage().getByClass(PatchFeature.class).b.set(true);
    }
}