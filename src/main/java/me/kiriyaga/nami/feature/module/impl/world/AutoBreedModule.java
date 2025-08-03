package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.core.rotation.*;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.interactWithEntity;

@RegisterModule
public class AutoBreedModule extends Module {

    private final DoubleSetting range = addSetting(new DoubleSetting("range", 2, 1.0, 5.0));
    private final IntSetting delay = addSetting(new IntSetting("delay", 10, 1, 20));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation", 3, 1, 10));

    private final Set<Integer> animalsFed = new HashSet<>();
    private int breedCooldown = 0;

    public AutoBreedModule() {
        super("auto breed", "Automatically breeds nearby animals.", ModuleCategory.of("world"), "autobreed", "фгещикуув");
    }

    @Override
    public void onDisable() {
        animalsFed.clear();
        breedCooldown = 0;
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        animalsFed.removeIf(id -> {
            Entity e = MC.world.getEntityById(id);
            return e == null || !e.isAlive() || MC.player.squaredDistanceTo(e) > range.get() * range.get();
        });

        if (breedCooldown > 0) {
            breedCooldown--;
            return;
        }

        for (Entity entity : ENTITY_MANAGER.getPassive()) {
            if (!(entity instanceof AnimalEntity animal)) continue;
            if (!animal.isAlive() || animal.isBaby() || animal.isInLove() || !animal.canEat()) continue;
            if (animalsFed.contains(animal.getId())) continue;

            double distance = MC.player.squaredDistanceTo(animal);
            if (distance > range.get() * range.get()) continue;

            int foodSlot = getBreedingItemSlot(animal);
            if (foodSlot == -1) continue;

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != foodSlot) {
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(foodSlot);
                breedCooldown = delay.get();
                return;
            }

            Vec3d center = getEntityCenter(animal);
            ROTATION_MANAGER.getRequestHandler().submit(
                    new RotationRequest(
                            AutoBreedModule.class.getName(),
                            rotationPriority.get(),
                            (float) getYawToVec(MC.player, center),
                            (float) getPitchToVec(MC.player, center)
                    )
            );

            if (!ROTATION_MANAGER.getRequestHandler().isCompleted(AutoBreedModule.class.getName())) return;

            interactWithEntity(animal, center, true);

            animalsFed.add(animal.getId());
            breedCooldown = delay.get();
            break;
        }
    }

    private int getBreedingItemSlot(AnimalEntity animal) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (!stack.isEmpty() && animal.isBreedingItem(stack)) {
                return i;
            }
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
