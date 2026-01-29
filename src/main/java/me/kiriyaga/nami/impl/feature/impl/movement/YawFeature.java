package me.kiriyaga.nami.impl.feature.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.IntSetting;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.util.RotationUtils.alignYaw;

@RegisterFeature
public class YawFeature extends Feature {

    private final IntSetting directions = addSetting(new IntSetting("Directions", 8, 4, 16));

    public YawFeature() {
        super("Yaw", "Snap player yaw to nearest fixed angle.", FeatureCategory.of("Movement"));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;

        float s = 360f / directions.get();
        float targetYaw = Math.round(MC.player.getYRot() / s) * s;

        targetYaw = alignYaw(targetYaw, MC.player.getYRot());
        MC.player.setYRot(targetYaw);
    }

}