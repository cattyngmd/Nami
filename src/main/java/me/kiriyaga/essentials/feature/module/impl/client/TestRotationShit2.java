package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.UpdateEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.manager.RotationManager;

import java.util.Random;

import static me.kiriyaga.essentials.Essentials.ROTATION_MANAGER;

public class TestRotationShit2 extends Module {
    private static final int CHANGE_INTERVAL = 40;
    private int tickCount = 0;
    private final Random random = new Random();

    private float targetYaw = 0f;
    private float targetPitch = 0f;

    public TestRotationShit2() {
        super("test insane shit", "Rotates player to random places every 40 ticks.", Category.CLIENT, "rotate", "spin", "circle", "test");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onUpdate(UpdateEvent event) {
        tickCount++;

        if (tickCount % CHANGE_INTERVAL == 0) {
            targetYaw = random.nextFloat() * 360f - 180f;
            targetPitch = random.nextFloat() * 180f - 90f;
        }

        ROTATION_MANAGER.submitRequest(new RotationManager.RotationRequest("123", 1, targetYaw, targetPitch));
    }
}