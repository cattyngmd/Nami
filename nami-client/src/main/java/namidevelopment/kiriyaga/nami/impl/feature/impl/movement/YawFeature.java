package namidevelopment.kiriyaga.nami.impl.feature.impl.movement;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;

import static namidevelopment.kiriyaga.nami.Nami.MC;
import static namidevelopment.kiriyaga.nami.util.RotationUtils.alignYaw;

@RegisterFeature
public class YawFeature extends Feature {

    public final IntSetting directions = addSetting(new IntSetting("Directions", 8, 4, 16));

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