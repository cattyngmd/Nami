package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.manager.RotationManager;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.ROTATION_MANAGER;

public class SpinnerModule extends Module {

    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation", 1, 1, 10));
    private final DoubleSetting rotationSpeed = addSetting(new DoubleSetting("speed", 5.0, 0.1, 50.0));
    private final DoubleSetting pitchSetting = addSetting(new DoubleSetting("pitch", 0.0, -90.0, 90.0));

    private float currentYaw = 0.0f;

    public SpinnerModule() {
        super("spinner", "Make you, spin!.", Category.movement, "ызшттук");
    }

    @Override
    public void onEnable() {
        currentYaw = MC.player != null ? MC.player.getYaw() : 0.0f;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null) return;

        currentYaw += rotationSpeed.get().floatValue();
        if (currentYaw > 360.0f) {
            currentYaw -= 360.0f;
        }

        float yaw = currentYaw;
        float pitch = pitchSetting.get().floatValue();

        ROTATION_MANAGER.submitRequest(new RotationManager.RotationRequest(
                SpinnerModule.class.getName(),
                rotationPriority.get(),
                yaw,
                pitch
        ));
    }
}
