package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.UpdateEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.manager.RotationManager;

import static me.kiriyaga.essentials.Essentials.ROTATION_MANAGER;

public class TestRotationShit2 extends Module {

    private float angle = 90f;

    public TestRotationShit2() {
        super("test insane shit", "Rotates player in a circle for testing rotation.", Category.CLIENT, "rotate", "spin", "circle", "test");
    }

    @SubscribeEvent
    private void onUpdate(UpdateEvent event) {

        ROTATION_MANAGER.submitRequest(new RotationManager.RotationRequest("123", 1, 90 , 0));
    }
}
