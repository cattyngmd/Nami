package namidevelopment.kiriyaga.nami.api.inventory;

import namidevelopment.kiriyaga.nami.impl.feature.impl.client.PatchFeature;
import namidevelopment.kiriyaga.nami.mixininterface.IClientPlayerInteractionManager;

import static namidevelopment.kiriyaga.nami.Nami.*;

public class InventorySlotHandler {

    // this shit was overengeneered and uncompat with like any client so i made it mc vanilla way
    public void attemptSwitch(int targetSlot) {
        if (MC.player == null || MC.level == null || MC.gameMode == null || targetSlot < 0 || targetSlot > 8)
            return;

        MC.player.getInventory().setSelectedSlot(targetSlot);
        syncSelectedSlot();
    }

    public void syncSelectedSlot(){
        ((IClientPlayerInteractionManager)MC.gameMode).updateSlot(); // this one is the same as mc default one

        if (FEATURE_SERVICE.getStorage().getByClass(PatchFeature.class).silentSwapFix.get())
            FEATURE_SERVICE.getStorage().getByClass(PatchFeature.class).b.set(true);
    }
}