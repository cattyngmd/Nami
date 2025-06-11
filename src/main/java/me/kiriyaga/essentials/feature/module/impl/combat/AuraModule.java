package me.kiriyaga.essentials.feature.module.impl.combat;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.Render3DEvent;
import me.kiriyaga.essentials.event.impl.UpdateEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.module.impl.client.ColorModule;
import me.kiriyaga.essentials.manager.RotationManager;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.setting.impl.EnumSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;
import me.kiriyaga.essentials.util.EntityUtils;
import me.kiriyaga.essentials.util.render.RenderUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static me.kiriyaga.essentials.Essentials.*;

public class AuraModule extends Module {
    public final DoubleSetting attackRange = addSetting(new DoubleSetting("Range", 4, 1, 6));
    public final BoolSetting render = addSetting(new BoolSetting("Render", true));


    private List<Entity> targets = new ArrayList<>();

    public AuraModule() {
        super("2b2t Aura(wip)", "----", Category.COMBAT, "killaura", "aura", "deathaura", "kilaura", "фгкф");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUpdate(UpdateEvent event) {
        if (MINECRAFT.player == null || MINECRAFT.world == null)
            return;

        Entity player = MINECRAFT.player;
        Entity target = getTarget();

        if (target == null) return;

        float cooldownProgress = MINECRAFT.player.getAttackCooldownProgress(0f);

        if (cooldownProgress < 1.0f)
            return;

        attack(target, getYawToEntity(player, target), getPitchToEntity(player, target));
    }



    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (!render.get() || getTarget() == null)
            return;

        ColorModule colorModule = MODULE_MANAGER.getModule(ColorModule.class);

        drawBox(getTarget(),colorModule.getStyledPrimaryColor(),event.getMatrices(), event.getTickDelta());
    }

    private void attack(Entity target, int yaw, int pitch) {
        if (ROTATION_MANAGER.isRequestCompleted(AuraModule.class.getName())) {
            MINECRAFT.interactionManager.attackEntity(MINECRAFT.player, target);
            MINECRAFT.player.swingHand(Hand.MAIN_HAND);
        } else {
            RotationManager.RotationRequest request = new RotationManager.RotationRequest(AuraModule.class.getName(),10, yaw, pitch);
            ROTATION_MANAGER.submitRequest(request);
        }
    }

    private Entity getTarget() {
        targets.clear();

        List<Entity> hostiles = EntityUtils.getHostileMobs();

        double maxRange = attackRange.get();
        ClientPlayerEntity player = MINECRAFT.player;

        targets = hostiles.stream()
                .filter(e -> !e.isRemoved() && e.squaredDistanceTo(player) <= maxRange * maxRange)
                .toList();

        return targets.stream()
                .min((e1, e2) -> Double.compare(e1.squaredDistanceTo(player), e2.squaredDistanceTo(player)))
                .orElse(null);
    }

    public static int getYawToEntity(Entity from, Entity to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        return wrapDegrees((int) Math.round(yaw));
    }

    public static int getPitchToEntity(Entity from, Entity to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() + to.getEyeHeight(to.getPose()) - (from.getY() + from.getEyeHeight(from.getPose()));
        double dz = to.getZ() - from.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        double pitch = -Math.toDegrees(Math.atan2(dy, distance));
        return (int) Math.round(pitch);
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