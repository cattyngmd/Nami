package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.interactWithEntity;

@RegisterModule
public class AutoFrameModule extends Module {

    private final DoubleSetting range = addSetting(new DoubleSetting("range", 4, 1.0, 6.0));
    private final IntSetting delay = addSetting(new IntSetting("delay", 10, 0, 20));
    private final BoolSetting rotate = addSetting(new BoolSetting("rotate", false));

    private int cooldown = 0;

    private final Set<Integer> clickedFrames = new HashSet<>();

    public AutoFrameModule() {
        super("auto frame", "Automatically puts a map in nearby item frames.", ModuleCategory.of("world"), "autoframe");
    }

    @Override
    public void onDisable() {
        cooldown = 0;
        clickedFrames.clear();
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        for (Entity entity : MC.world.getEntities()) {
            if (!(entity instanceof ItemFrameEntity frame)) continue;

            if (clickedFrames.contains(frame.getId())) continue;

            if (MC.player.squaredDistanceTo(frame) > range.get() * range.get()) continue;

            int mapSlot = getMapSlot();
            if (mapSlot == -1) continue;

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != mapSlot) {
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(mapSlot);
                cooldown = delay.get();
                return;
            }

            Vec3d center = getEntityCenter(frame);

            if (rotate.get()) {
                ROTATION_MANAGER.getRequestHandler().submit(
                        new RotationRequest(
                                AutoFrameModule.class.getName(),
                                2,
                                (float) getYawToVec(MC.player, center),
                                (float) getPitchToVec(MC.player, center)
                        )
                );
                if (!ROTATION_MANAGER.getRequestHandler().isCompleted(AutoFrameModule.class.getName())) return;
            }

            interactWithEntity(frame, center, true);

            clickedFrames.add(frame.getId());

            cooldown = delay.get();
            break;
        }
    }

    private int getMapSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof FilledMapItem) {
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