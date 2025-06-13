package me.kiriyaga.essentials.feature.module.impl.combat;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PreTickEvent;
import me.kiriyaga.essentials.event.impl.Render3DEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.module.impl.client.ColorModule;
import me.kiriyaga.essentials.manager.RotationManager;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.util.EntityUtils;
import me.kiriyaga.essentials.util.render.RenderUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static me.kiriyaga.essentials.Essentials.*;

public class AuraModule extends Module {

    public final DoubleSetting attackRange = addSetting(new DoubleSetting("Attack", 3.0, 1.0, 5));
    public final DoubleSetting rotationRange = addSetting(new DoubleSetting("Rotation", 3.6, 2.0, 6.0));
    public final BoolSetting swordOnly = addSetting(new BoolSetting("Weap only", false));
    public final BoolSetting render = addSetting(new BoolSetting("Render", true));
    public final BoolSetting tpsSync = addSetting(new BoolSetting("TPS", true));
    public final BoolSetting multiTask = addSetting(new BoolSetting("MultiTask", false));
    public final DoubleSetting minTicksExisted = addSetting(new DoubleSetting("Age", 12, 0.0, 20.0));

    public final BoolSetting targetPlayers = addSetting(new BoolSetting("Players", true));
    public final BoolSetting targetPeacefuls = addSetting(new BoolSetting("Peacefuls", false));
    public final BoolSetting targetHostiles = addSetting(new BoolSetting("Hostiles", true));
    public final BoolSetting targetNeutrals = addSetting(new BoolSetting("Neutrals", false));

    public AuraModule() {
        super("Aura", "Attack entities for you.", Category.COMBAT, "killaura", "ara", "killara", "фгкф");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onTick(PreTickEvent event) {
        if (MINECRAFT.player == null || MINECRAFT.world == null) return;
        if (!multiTask.get() && MINECRAFT.player.isUsingItem()) return;

        Entity target = getTarget(attackRange.get());
        if (target == null) return;

        if (swordOnly.get()) {
            ItemStack stack = MINECRAFT.player.getMainHandStack();
            if (!(stack.getItem() instanceof AxeItem || stack.isIn(ItemTags.SWORDS))) return;
        }

        float cooldown = MINECRAFT.player.getAttackCooldownProgress(0f);
        if (tpsSync.get()) {
            float tps = 20f;
            if (MINECRAFT.getServer() != null) {
                double tickTimeNs = MINECRAFT.getServer().getAverageTickTime();
                double tickTimeMs = tickTimeNs / 1_000_000.0;
                tps = (float) Math.min(20.0, 1000.0 / tickTimeMs);
            }
            float tpsFactor = 20f / tps;
            if (cooldown < 1.0f * tpsFactor) return;
        } else {
            if (cooldown < 1.0f) return;
        }

        int yaw = getYawToEntity(MINECRAFT.player, target);
        int pitch = getPitchToEntity(MINECRAFT.player, target);

        attack(target, yaw, pitch);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (!render.get()) return;

        Entity target = getTarget(rotationRange.get());
        if (target == null) return;

        ColorModule colorModule = MODULE_MANAGER.getModule(ColorModule.class);
        drawBox(target, colorModule.getStyledPrimaryColor(), event.getMatrices(), event.getTickDelta());
    }

    private Entity getTarget(double range) {
        ClientPlayerEntity player = MINECRAFT.player;
        List<Entity> candidates = new ArrayList<>();

        if (targetPlayers.get()) candidates.addAll(EntityUtils.getPlayers());
        if (targetPeacefuls.get()) candidates.addAll(EntityUtils.getPassiveMobs());
        if (targetHostiles.get()) candidates.addAll(EntityUtils.getHostileMobs());
        if (targetNeutrals.get()) candidates.addAll(EntityUtils.getNeutralMobs());

        candidates.removeIf(e -> e == player
                || e.isRemoved()
                || !e.isAlive()
                || e.squaredDistanceTo(player) > range * range
                || e.age < minTicksExisted.get().intValue());

        return candidates.stream()
                .min((e1, e2) -> Double.compare(e1.squaredDistanceTo(player), e2.squaredDistanceTo(player)))
                .orElse(null);
    }

    private void attack(Entity target, int yaw, int pitch) {
        if (ROTATION_MANAGER.isRequestCompleted(AuraModule.class.getName())) {
            MINECRAFT.interactionManager.attackEntity(MINECRAFT.player, target);
            MINECRAFT.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        } else {
            RotationManager.RotationRequest request = new RotationManager.RotationRequest(AuraModule.class.getName(), 10, yaw, pitch);
            ROTATION_MANAGER.submitRequest(request);
        }
    }

    private void drawBox(Entity entity, Color color, MatrixStack matrices, float partialTicks) {
        double interpX = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * partialTicks;
        double interpY = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * partialTicks;
        double interpZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * partialTicks;

        Box box = entity.getBoundingBox().offset(interpX - entity.getX(), interpY - entity.getY(), interpZ - entity.getZ());
        RenderUtil.drawBoxFilled(matrices, box, new Color(color.getRed(), color.getGreen(), color.getBlue(), 75));
    }

    /**
     * I think mc server doesnt actually get`s your info, about attacking, since there is no any vec sended, but it checks your actual
     * rotations, and based on that could allow you attack a bit further ?
     * not really sure but i made it, why not
     */
    public static int getYawToEntity(Entity from, Entity to) {
        Vec3d fromPos = from.getPos().add(0, from.getEyeHeight(from.getPose()), 0);

        Vec3d feet = new Vec3d(to.getX(), to.getY(), to.getZ());
        Vec3d body = to.getBoundingBox().getCenter();
        Vec3d head = new Vec3d(to.getX(), to.getY() + to.getEyeHeight(to.getPose()), to.getZ());

        Vec3d[] points = {feet, body, head};

        Vec3d closestPoint = points[0];
        double minDist = fromPos.distanceTo(points[0]);
        for (Vec3d point : points) {
            double dist = fromPos.distanceTo(point);
            if (dist < minDist) {
                minDist = dist;
                closestPoint = point;
            }
        }

        double dx = closestPoint.x - fromPos.x;
        double dz = closestPoint.z - fromPos.z;
        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        return wrapDegrees((int) Math.round(yaw));
    }

    public static int getPitchToEntity(Entity from, Entity to) {
        Vec3d fromPos = from.getPos().add(0, from.getEyeHeight(from.getPose()), 0);

        Vec3d feet = new Vec3d(to.getX(), to.getY(), to.getZ());
        Vec3d body = to.getBoundingBox().getCenter();
        Vec3d head = new Vec3d(to.getX(), to.getY() + to.getEyeHeight(to.getPose()), to.getZ());

        Vec3d[] points = {feet, body, head};

        Vec3d closestPoint = points[0];
        double minDist = fromPos.distanceTo(points[0]);
        for (Vec3d point : points) {
            double dist = fromPos.distanceTo(point);
            if (dist < minDist) {
                minDist = dist;
                closestPoint = point;
            }
        }

        double dx = closestPoint.x - fromPos.x;
        double dy = closestPoint.y - fromPos.y;
        double dz = closestPoint.z - fromPos.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);

        return (int) Math.round(-Math.toDegrees(Math.atan2(dy, distXZ)));
    }

    private static int wrapDegrees(int angle) {
        angle %= 360;
        if (angle >= 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
}
