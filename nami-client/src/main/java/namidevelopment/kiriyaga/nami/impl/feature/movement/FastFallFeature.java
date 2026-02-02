package namidevelopment.kiriyaga.nami.impl.feature.movement;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import net.minecraft.world.phys.Vec3;

import static namidevelopment.kiriyaga.nami.Nami.INPUT_SERVICE;
import static namidevelopment.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class FastFallFeature extends Feature {

    public final DoubleSetting speed = addSetting(new DoubleSetting("Speed", 1.00, 0.10, 5.00));

    public FastFallFeature() {
        super("FastFall", "Fall from blocks faster.", FeatureCategory.of("Movement"), "fastfall");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onTick(PreTickEvent ev) {
        if (MC.level == null || MC.player == null) return;

        if (!INPUT_SERVICE.hasAnyInput() || INPUT_SERVICE.isJumpPressed() || MC.player.isInPowderSnow || MC.player.isUnderWater() || MC.player.isInLava() || MC.player.isInWater() || MC.player.isFallFlying() || MC.player.isNoGravity())
            return;

        if (MC.player.onGround()) {
            Vec3 prev = MC.player.getDeltaMovement();
            MC.player.setDeltaMovement(prev.x, -speed.get(), prev.z);
        }
    }
}
