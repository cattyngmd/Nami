package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.ROTATION_MANAGER;

@RegisterModule
public class AntiAimModule extends Module {

    private final DoubleSetting rotationSpeed = addSetting(new DoubleSetting("speed", 5.0, 0.1, 50.0));
    private final DoubleSetting pitchSetting = addSetting(new DoubleSetting("pitch", 0.0, -90.0, 90.0));

    private float currentYaw = 0.0f;

    public AntiAimModule() {
        super("anti aim", "Make you, spin!.", ModuleCategory.of("movement"), "antiaim");
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

        ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(
                AntiAimModule.class.getName(),
                0,
                yaw,
                pitch
        ));
    }
}
