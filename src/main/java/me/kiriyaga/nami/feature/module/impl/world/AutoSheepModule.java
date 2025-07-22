package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.core.RotationManager;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.ClientPlayerInteractionManagerAccessor;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class AutoSheepModule extends Module {

    private final DoubleSetting shearRange = addSetting(new DoubleSetting("range", 2.5, 1.0, 5.0));
    private final IntSetting delay = addSetting(new IntSetting("delay", 5, 1, 20));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation", 2, 1, 10));

    private int swapCooldown = 0;

    public AutoSheepModule() {
        super("auto sheep", "Automatically shears nearby sheep.", ModuleCategory.of("world"), "sheep", "autowool", "фгещырууз");
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (swapCooldown > 0) {
            swapCooldown--;
            return;
        }

        for (Entity entity : ENTITY_MANAGER.getPassive()) {
            if (!(entity instanceof SheepEntity sheep)) continue;
            if (!sheep.isAlive() || sheep.isSheared() || sheep.isBaby()) continue;

            double distance = MC.player.squaredDistanceTo(sheep);
            if (distance > shearRange.get() * shearRange.get()) continue;

            int shearsSlot = getShearsSlot();
            if (shearsSlot == -1) continue;

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != shearsSlot) {
                selectHotbarSlotImmediate(shearsSlot);
                swapCooldown = delay.get();
                return;
            }

            Vec3d center = getEntityCenter(sheep);
            ROTATION_MANAGER.submitRequest(
                    new RotationManager.RotationRequest(
                            AutoSheepModule.class.getName(),
                            rotationPriority.get(),
                            (float) getYawToVec(MC.player, center),
                            (float) getPitchToVec(MC.player, center)
                    )
            );

            if (!ROTATION_MANAGER.isRequestCompleted(AutoSheepModule.class.getName())) return;

            MC.interactionManager.interactEntity(MC.player, sheep, Hand.MAIN_HAND);
            swapCooldown = delay.get();
            break;
        }
    }

    private int getShearsSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == Items.SHEARS) return i;
        }
        return -1;
    }

    private static boolean selectHotbarSlotImmediate(int slot) {
        if (slot < 0 || slot > 8) return false;
        MC.player.getInventory().setSelectedSlot(slot);
        ((ClientPlayerInteractionManagerAccessor) MC.interactionManager).callSyncSelectedSlot();
        return true;
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
