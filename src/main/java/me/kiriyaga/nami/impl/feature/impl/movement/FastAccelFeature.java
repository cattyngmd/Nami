package me.kiriyaga.nami.impl.feature.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.MoveEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

import static me.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class FastAccelFeature extends Feature {

    private final BoolSetting inAir = addSetting(new BoolSetting("InAir", false));
    private final BoolSetting inWater = addSetting(new BoolSetting("InWater", false));
    private final DoubleSetting speed = addSetting(new DoubleSetting("Speed", 1.0, 0.1, 5.0));
    private static final double DEFAULT_SPEED = 0.2873;

    public FastAccelFeature() {
        super("FastAccel", "Accelerates movement instantly.", FeatureCategory.of("Movement"), "fastaccel");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onMoveEvent(MoveEvent event) {
        if (MC.player == null || MC.level == null) return;

        if (MC.player.fallDistance >= 3.0F || MC.player.isShiftKeyDown() || MC.player.onClimbable() || (MC.player.isInWater() && !inWater.get()))
            return;

        if (!MC.player.onGround() && !inAir.get())
            return;

        double finalSpeed = DEFAULT_SPEED;
        if (MC.player.hasEffect(MobEffects.SPEED)) {
            int amplifier = MC.player.getEffect(MobEffects.SPEED).getAmplifier();
            finalSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }

        finalSpeed *= speed.get();

        float forward = MC.player.input.getMoveVector().y;
        float sideways = MC.player.input.getMoveVector().x;
        float yaw = MC.player.getYRot();

        if (forward != 0.0f) {
            if (sideways > 0.0f)
                yaw += (forward > 0.0f) ? -45 : 45;
            else if (sideways < 0.0f)
                yaw += (forward > 0.0f) ? 45 : -45;

            sideways = 0.0f;

            if (forward > 0.0f) forward = 1.0f;
            else if (forward < 0.0f) forward = -1.0f;
        }

        double magnitude = Math.sqrt(forward * forward + sideways * sideways);
        if (magnitude < 1.0f && magnitude > 0.0f) {
            forward /= (float) magnitude;
            sideways /= (float) magnitude;
        }

        double motionX = Math.cos(Math.toRadians(yaw + 90.0f));
        double motionZ = Math.sin(Math.toRadians(yaw + 90.0f));

        Vec3 motion = new Vec3(forward * finalSpeed * motionX + sideways * finalSpeed * motionZ, 0, forward * finalSpeed * motionZ - sideways * finalSpeed * motionX);
        event.setMovement(new Vec3(motion.x, event.getMovement().y, motion.z));
    }
}
