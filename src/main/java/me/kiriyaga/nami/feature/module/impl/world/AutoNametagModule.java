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
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.interactWithEntity;

@RegisterModule
public class AutoNametagModule extends Module {

    private final BoolSetting nametagged = addSetting(new BoolSetting("nametagged", false));
    private final DoubleSetting range = addSetting(new DoubleSetting("range", 5.0, 1.0, 10.0));
    private final IntSetting delay = addSetting(new IntSetting("delay", 10, 1, 20));
    private final BoolSetting rotate = addSetting(new BoolSetting("rotate", true));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation", 3, 1, 10));

    private int swapCooldown = 0;

    public AutoNametagModule() {
        super("auto nametag", "Automatically renames nearby entities with nametags.", ModuleCategory.of("world"), "nametag", "autoname", "autonametag");
        rotationPriority.setShowCondition(rotate::get);
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (swapCooldown > 0) {
            swapCooldown--;
            return;
        }

        for (Entity entity : MC.world.getEntities()) {
            if (entity == null || entity == MC.player) continue;
            if (entity.getCustomName() != null && !nametagged.get()) continue;
            if (entity instanceof VillagerEntity || entity instanceof EnderPearlEntity || entity instanceof EnderDragonEntity) continue;

            double distance = MC.player.squaredDistanceTo(entity);
            if (distance > range.get() * range.get()) continue;

            int nameTagSlot = getNameTagSlot();
            if (nameTagSlot == -1) continue;

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != nameTagSlot) {
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(nameTagSlot);
                swapCooldown = delay.get();
                return;
            }

            Vec3d center = getEntityCenter(entity);

            if (rotate.get()) {
                ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(
                        AutoNametagModule.class.getName(),
                        rotationPriority.get(),
                        (float) getYawToVec(MC.player, center),
                        (float) getPitchToVec(MC.player, center)
                ));

                if (!ROTATION_MANAGER.getRequestHandler().isCompleted(AutoNametagModule.class.getName())) return;
            }

            interactWithEntity(entity, center, true);

            swapCooldown = delay.get();
            break;
        }
    }

    private int getNameTagSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == Items.NAME_TAG) return i;
        }
        return -1;
    }

    private static Vec3d getEntityCenter(Entity entity) {
        Box box = entity.getBoundingBox();
        double centerX = box.minX + box.getLengthX() / 2;
        double centerY = box.minY + box.getLengthY() / 2;
        double centerZ = box.minZ + box.getLengthZ() / 2;
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