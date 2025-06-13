package me.kiriyaga.essentials.feature.module.impl.combat;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.Render3DEvent;
import me.kiriyaga.essentials.event.impl.PreTickEvent;
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
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.List;

import static me.kiriyaga.essentials.Essentials.*;

public class AuraModule extends Module {
    public final DoubleSetting attackRange = addSetting(new DoubleSetting("Range", 4, 1, 6));
    public final BoolSetting swordOnly = addSetting(new BoolSetting("Sword Only", false));
    public final BoolSetting render = addSetting(new BoolSetting("Render", true));

    public AuraModule() {
        super("2b2t Aura(wip)", "----", Category.COMBAT, "killaura", "aura", "deathaura", "kilaura", "фгкф");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUpdate(PreTickEvent event) {

        if (MINECRAFT.player == null || MINECRAFT.world == null)
            return;

        Entity player = MINECRAFT.player;
        Entity target = getTarget();

        if (target == null)
            return;

        if (swordOnly.get()) {
            Item item = MINECRAFT.player.getMainHandStack().getItem();
            boolean isSwordOrAxe = item == Items.WOODEN_SWORD || item == Items.STONE_SWORD ||
                    item == Items.IRON_SWORD || item == Items.GOLDEN_SWORD ||
                    item == Items.DIAMOND_SWORD || item == Items.NETHERITE_SWORD ||
                    item == Items.WOODEN_AXE || item == Items.STONE_AXE ||
                    item == Items.IRON_AXE || item == Items.GOLDEN_AXE ||
                    item == Items.DIAMOND_AXE || item == Items.NETHERITE_AXE;

            if (!isSwordOrAxe)
                return;
        }


        float cooldownProgress = MINECRAFT.player.getAttackCooldownProgress(0f);

        if (cooldownProgress < 1.0f)
            return;

        int yaw = getYawToEntity(player, target);
        int pitch = getPitchToEntity(player, target);

        attack(target, yaw, pitch);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (!render.get())
            return;


        Entity target = getTarget();

        if (target == null)
            return;


        ColorModule colorModule = MODULE_MANAGER.getModule(ColorModule.class);
        drawBox(target, colorModule.getStyledPrimaryColor(), event.getMatrices(), event.getTickDelta());
    }

    private void attack(Entity target, int yaw, int pitch) {
        if (ROTATION_MANAGER.isRequestCompleted(AuraModule.class.getName())) {
            MINECRAFT.interactionManager.attackEntity(MINECRAFT.player, target);
            MINECRAFT.player.swingHand(Hand.MAIN_HAND);
        } else {
            RotationManager.RotationRequest request = new RotationManager.RotationRequest(AuraModule.class.getName(), 10, yaw, pitch);
            ROTATION_MANAGER.submitRequest(request);
        }
    }

    private Entity getTarget() {
        List<Entity> targets;

        List<Entity> hostiles = EntityUtils.getHostileMobs();

        double maxRange = attackRange.get();
        ClientPlayerEntity player = MINECRAFT.player;


        targets = hostiles.stream()
                .filter(e -> {
                    boolean alive = e.isAlive();
                    double distSq = e.squaredDistanceTo(player);
                    boolean inRange = distSq <= maxRange * maxRange;
                    return alive && inRange;
                })
                .toList();

        Entity closest = targets.stream()
                .min((e1, e2) -> Double.compare(e1.squaredDistanceTo(player), e2.squaredDistanceTo(player)))
                .orElse(null);

        return closest;
    }

    public static int getYawToEntity(Entity from, Entity to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        int wrapped = wrapDegrees((int) Math.round(yaw));
        return wrapped;
    }

    public static int getPitchToEntity(Entity from, Entity to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() + to.getEyeHeight(to.getPose()) - (from.getY() + from.getEyeHeight(from.getPose()));
        double dz = to.getZ() - from.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        double pitch = -Math.toDegrees(Math.atan2(dy, distance));
        int pitchInt = (int) Math.round(pitch);
        return pitchInt;
    }

    private static int wrapDegrees(int angle) {
        angle %= 360;
        if (angle >= 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }

    private void drawBox(Entity entity, Color color, MatrixStack matrices, float partialTicks) {

        double interpX = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * partialTicks;
        double interpY = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * partialTicks;
        double interpZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * partialTicks;

        Box box = entity.getBoundingBox().offset(
                interpX - entity.getX(),
                interpY - entity.getY(),
                interpZ - entity.getZ()
        );

        RenderUtil.drawBoxFilled(matrices, box, new Color(color.getRed(), color.getGreen(), color.getBlue(), 75));
    }
}
