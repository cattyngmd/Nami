package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.core.RotationManager;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.interactWithEntity;

@RegisterModule
public class AutoMountModule extends Module {

    private final DoubleSetting range = addSetting(new DoubleSetting("range", 2, 1.0, 10.0));
    private final IntSetting delay = addSetting(new IntSetting("delay", 10, 1, 20));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation", 3, 1, 10));

    private int actionCooldown = 0;

    public AutoMountModule() {
        super("auto mount", "Automatically mounts nearby entities.", ModuleCategory.of("world"), "mount", "automount", "фгещьщгте");
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (MC.player.hasVehicle()) return;

        if (actionCooldown > 0) {
            actionCooldown--;
            return;
        }

        for (Entity entity : MC.world.getEntities()) {
            if (entity == null || entity == MC.player || !entity.isAlive() || entity.hasPassengers()) continue;

            if (!(entity instanceof HorseEntity || entity instanceof PigEntity || entity instanceof StriderEntity ||
                    entity instanceof LlamaEntity || entity instanceof DonkeyEntity ||
                    entity instanceof BoatEntity || entity instanceof MinecartEntity)) continue;

            double distSq = MC.player.squaredDistanceTo(entity);
            if (distSq > range.get() * range.get()) continue;

            Vec3d center = getEntityCenter(entity);
            ROTATION_MANAGER.submitRequest(
                    new RotationManager.RotationRequest(
                            AutoMountModule.class.getName(),
                            rotationPriority.get(),
                            (float) getYawToVec(MC.player, center),
                            (float) getPitchToVec(MC.player, center)
                    )
            );

            if (!ROTATION_MANAGER.isRequestCompleted(AutoMountModule.class.getName())) return;

            interactWithEntity(entity, center, true);

            actionCooldown = delay.get();
            break;
        }
    }

    private static Vec3d getEntityCenter(Entity entity) {
        Box box = entity.getBoundingBox();
        double centerX = box.minX + (box.getLengthX() / 2);
        double centerY = box.minY + (box.getLengthY() / 2);
        double centerZ = box.minZ + (box.getLengthZ() / 2);
        return new Vec3d(centerX, centerY, centerZ);
    }

    private static int getYawToVec(Entity from, Vec3d to) {
        double dx = to.x - from.getX();
        double dz = to.z - from.getZ();
        return wrapDegrees((int) Math.round(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
    }

    private static int getPitchToVec(Entity from, Vec3d to) {
        Vec3d eyePos = from.getEyePos();
        double dx = to.x - eyePos.x;
        double dy = to.y - eyePos.y;
        double dz = to.z - eyePos.z;
        return (int) Math.round(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
    }

    private static int wrapDegrees(int angle) {
        angle %= 360;
        if (angle >= 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
}
