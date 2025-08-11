package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.core.rotation.*;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.interactWithEntity;

@RegisterModule
public class AutoCowModule extends Module {

    private final DoubleSetting milkRange = addSetting(new DoubleSetting("range", 2.5, 1.0, 5.0));
    private final IntSetting delay = addSetting(new IntSetting("delay", 5, 1, 20));
    private final BoolSetting rotate = addSetting(new BoolSetting("rotate", false));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation", 2, 1, 10));

    private int swapCooldown = 0;

    public AutoCowModule() {
        super("auto cow", "Automatically milks nearby cows.", ModuleCategory.of("world"), "cow", "milk", "autocow");
        rotationPriority.setShowCondition(rotate::get);
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (swapCooldown > 0) {
            swapCooldown--;
            return;
        }

        for (Entity entity : ENTITY_MANAGER.getPassive()) {
            if (!(entity instanceof CowEntity cow)) continue;
            if (!cow.isAlive() || cow.isBaby()) continue;

            double distance = MC.player.squaredDistanceTo(cow);
            if (distance > milkRange.get() * milkRange.get()) continue;

            int bucketSlot = getBucketSlot();
            if (bucketSlot == -1) continue;

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != bucketSlot) {
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(bucketSlot);
                swapCooldown = delay.get();
                return;
            }

            Vec3d center = getEntityCenter(cow);

            if (rotate.get()) {
                ROTATION_MANAGER.getRequestHandler().submit(
                        new RotationRequest(
                                AutoCowModule.class.getName(),
                                rotationPriority.get(),
                                (float) getYawToVec(MC.player, center),
                                (float) getPitchToVec(MC.player, center)
                        )
                );

                if (!ROTATION_MANAGER.getRequestHandler().isCompleted(AutoCowModule.class.getName())) return;
            }

            interactWithEntity(entity, center, true);
            swapCooldown = delay.get();
            break;
        }
    }

    private int getBucketSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == Items.BUCKET) return i;
        }
        return -1;
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
