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
import me.kiriyaga.essentials.setting.impl.IntSetting;
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

    public final DoubleSetting attackRange = addSetting(new DoubleSetting("attack", 3.0, 1.0, 5));
    public final DoubleSetting rotationRange = addSetting(new DoubleSetting("rotation", 3.6, 2.0, 6.0));
    public final BoolSetting swordOnly = addSetting(new BoolSetting("weap only", false));
    public final BoolSetting render = addSetting(new BoolSetting("render", true));
    public final BoolSetting tpsSync = addSetting(new BoolSetting("tps sync", true));
    public final BoolSetting multiTask = addSetting(new BoolSetting("multitask", false));
    public final DoubleSetting minTicksExisted = addSetting(new DoubleSetting("age", 12, 0.0, 20.0));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation", 5, 1, 30));
    public final BoolSetting targetPlayers = addSetting(new BoolSetting("players", true));
    public final BoolSetting targetPeacefuls = addSetting(new BoolSetting("peacefuls", false));
    public final BoolSetting targetHostiles = addSetting(new BoolSetting("hostiles", true));
    public final BoolSetting targetNeutrals = addSetting(new BoolSetting("neutrals", false));

    private Entity currentTarget = null;

    public AuraModule() {
        super("aura", "Attack entities for you.", Category.COMBAT, "killaura", "ara", "killara", "фгкф");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onTick(PreTickEvent event) {
        if (MINECRAFT.player == null || MINECRAFT.world == null) return;
        if (!multiTask.get() && MINECRAFT.player.isUsingItem()) return;

        if (swordOnly.get()) {
            ItemStack stack = MINECRAFT.player.getMainHandStack();
            if (!(stack.getItem() instanceof AxeItem || stack.isIn(ItemTags.SWORDS))) return;
        }

        Entity target = getTarget(rotationRange.get());
        if (target == null) {
            currentTarget = null;
            return;
        }

        currentTarget = target;

        int yaw = getYawToEntity(MINECRAFT.player, target);
        int pitch = getPitchToEntity(MINECRAFT.player, target);

        RotationManager.RotationRequest request = new RotationManager.RotationRequest(AuraModule.class.getName(), rotationPriority.get(), yaw, pitch);
        ROTATION_MANAGER.submitRequest(request);

        double distanceToTarget = MINECRAFT.player.squaredDistanceTo(target);
        if (distanceToTarget > attackRange.get() * attackRange.get()) {
            return;
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

        if (!ROTATION_MANAGER.isRequestCompleted(AuraModule.class.getName())) return;

        MINECRAFT.interactionManager.attackEntity(MINECRAFT.player, target);
        MINECRAFT.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
    }



    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (!render.get()) return;
        if (currentTarget == null) return;

        ColorModule colorModule = MODULE_MANAGER.getModule(ColorModule.class);
        drawBox(currentTarget, colorModule.getStyledPrimaryColor(), event.getMatrices(), event.getTickDelta());
    }


    private Entity getTarget(double range) {
        ClientPlayerEntity player = MINECRAFT.player;
        List<Entity> candidates = new ArrayList<>();

        if (targetPlayers.get()) candidates.addAll(EntityUtils.getPlayers());
        if (targetPeacefuls.get()) candidates.addAll(EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PASSIVE));
        if (targetHostiles.get()) candidates.addAll(EntityUtils.getEntities(EntityUtils.EntityTypeCategory.HOSTILE));
        if (targetNeutrals.get()) candidates.addAll(EntityUtils.getEntities(EntityUtils.EntityTypeCategory.NEUTRAL));

        candidates.removeIf(e -> e == player
                || e.isRemoved()
                || !e.isAlive()
                || e.squaredDistanceTo(player) > range * range
                || e.age < minTicksExisted.get().intValue());

        return candidates.stream()
                .min((e1, e2) -> Double.compare(e1.squaredDistanceTo(player), e2.squaredDistanceTo(player)))
                .orElse(null);
    }

    private void drawBox(Entity entity, Color color, MatrixStack matrices, float partialTicks) {
        double interpX = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * partialTicks;
        double interpY = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * partialTicks;
        double interpZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * partialTicks;

        Box box = entity.getBoundingBox().offset(interpX - entity.getX(), interpY - entity.getY(), interpZ - entity.getZ());
        RenderUtil.drawBoxFilled(matrices, box, new Color(color.getRed(), color.getGreen(), color.getBlue(), 75));
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
}
