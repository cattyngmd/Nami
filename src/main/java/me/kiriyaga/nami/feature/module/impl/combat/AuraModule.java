package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.core.rotation.*;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.Debug;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.util.hit.EntityHitResult;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class AuraModule extends Module {

    public enum TpsMode { NONE, LATEST, AVERAGE }

    public final DoubleSetting rotateRange = addSetting(new DoubleSetting("rotate", 3.00, 1.0, 6.0));
    public final DoubleSetting attackRange = addSetting(new DoubleSetting("attack", 3.00, 1.0, 6.0));
    public final BoolSetting vanillaRange = addSetting(new BoolSetting("vanilla range", true));
    public final BoolSetting swordOnly = addSetting(new BoolSetting("weap only", false));
    public final BoolSetting render = addSetting(new BoolSetting("render", true));
    public final EnumSetting<TpsMode> tpsMode = addSetting(new EnumSetting<>("tps", TpsMode.NONE));
    public final BoolSetting multiTask = addSetting(new BoolSetting("multitask", false));
    public final BoolSetting stopSprinting = addSetting(new BoolSetting("stop sprinting", true));
    public final BoolSetting raycast = addSetting(new BoolSetting("raycast", true));
    public final BoolSetting raycastConfirm = addSetting(new BoolSetting("raycast confirm", true));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation", 5, 1, 10));
    public final DoubleSetting preRotate = addSetting(new DoubleSetting("pre rotate", 0.1, 0.0, 1.0));

    private Entity currentTarget = null;
    private float attackCooldownTicks = 0f;

    public AuraModule() {
        super("aura", "Attacks certain targets automatically.", ModuleCategory.of("combat"), "killaura", "ara", "killara");
    raycastConfirm.setShowCondition(raycast::get);
    }

    @Override
    public void onDisable() {
        currentTarget = null;
        //ROTATION_MANAGER.cancelRequest(AuraModule.class.getName()); //no
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;
        if (!multiTask.get() && MC.player.isUsingItem()) return;

        long startTime = System.nanoTime();

        ItemStack stack = MC.player.getMainHandStack();
        Entity target = ENTITY_MANAGER.getTarget();
        Debug debugModule = MODULE_MANAGER.getStorage().getByClass(Debug.class);

        if (target == null || (swordOnly.get() && !(stack.getItem() instanceof AxeItem
                || stack.isIn(ItemTags.SWORDS)
                || stack.getItem() instanceof TridentItem
                || stack.getItem() instanceof MaceItem))) {
            currentTarget = null;
            this.setDisplayInfo("");
            return;
        }

        currentTarget = target;
        this.setDisplayInfo(target.getName().getString());
        long auraLogicStart = System.nanoTime();

        float tps;
        switch (tpsMode.get()) {
            case LATEST -> tps = TICK_MANAGER.getLatestTPS();
            case AVERAGE -> tps = TICK_MANAGER.getAverageTPS();
            default -> tps = 20f;
        }

//        if (!ItemStack.areEqual(stack, lastHeldStack)) {
//            lastHeldStack = stack;
//            attackCooldownTicks = getBaseCooldownTicks(stack, tps);
//        }

        attackCooldownTicks -= 1f * (tps / 20f);
        if (attackCooldownTicks < 0f) attackCooldownTicks = 0f;

        boolean skipCooldown = false;

        if (target instanceof ShulkerBulletEntity) {
            skipCooldown = true;
        } else {
            ItemStack held = stack;
            float attackDamage = 1.0f;

            if (MC.player.hasStatusEffect(StatusEffects.STRENGTH)) {
                var strength = MC.player.getStatusEffect(StatusEffects.STRENGTH);
                attackDamage += 3.0f * (strength.getAmplifier() + 1);
            }

            if (MC.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
                var weakness = MC.player.getStatusEffect(StatusEffects.WEAKNESS);
                attackDamage -= 4.0f * (weakness.getAmplifier() + 1); // im not sure is it 4 or 3 btw
            }

            if (target instanceof LivingEntity living) {
                if (living.getMaxHealth() <= attackDamage) {
                    skipCooldown = true;
                }
            }
        }

        //        if (MC.player.isGliding() && eyeDist >= 2.601) // yes
        //            return;

        double eyeDist = getClosestEyeDistance(MC.player.getEyePos(), target.getBoundingBox());

        if (eyeDist <= rotateRange.get() && (skipCooldown || attackCooldownTicks <= preRotate.get() * tps)) {
            Vec3d rotationTarget;
            if (raycast.get()) {
                Vec3d eyePos = MC.player.getCameraPosVec(1.0f);
                rotationTarget = getClosestPointToEye(eyePos, target.getBoundingBox());
            } else {
                rotationTarget = getEntityCenter(target);
            }

            ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(
                    AuraModule.class.getName(),
                    rotationPriority.get(),
                    (float) getYawToVec(MC.player, rotationTarget),
                    (float) getPitchToVec(MC.player, rotationTarget)
            ));
        }

        if (!ROTATION_MANAGER.getRequestHandler().isCompleted(AuraModule.class.getName()) && (!raycast.get() || !raycastConfirm.get()))
            return;

        boolean canAttack;
        if (raycast.get()) {
            EntityHitResult attackHit;
            if (raycastConfirm.get()) {
                attackHit = raycastTarget(MC.player, target, attackRange.get(),
                        ROTATION_MANAGER.getStateHandler().getRotationYaw(),
                        ROTATION_MANAGER.getStateHandler().getRotationPitch());
            } else {
                Vec3d eyePos = MC.player.getEyePos();
                Vec3d closestPoint = getClosestPointToEye(eyePos, target.getBoundingBox());
                float idealYaw = (float) getYawToVec(MC.player, closestPoint);
                float idealPitch = (float) getPitchToVec(MC.player, closestPoint);
                attackHit = raycastTarget(MC.player, target, attackRange.get(), idealYaw, idealPitch);
            }
            boolean insideBox = target.getBoundingBox().contains(MC.player.getEyePos()); // i dont fucking know why raycast doesnt apply while inside AABB, i thought mc aabb fixes it
            canAttack = insideBox || (attackHit != null && attackHit.getEntity() == target);
        } else {
            double dist = MC.player.getPos().distanceTo(getEntityCenter(target));
            canAttack = dist <= attackRange.get();
        }

        if (!canAttack) return;
        if (!skipCooldown && attackCooldownTicks > 0f) return;

        if (stopSprinting.get())
            MC.player.setSprinting(false);

        MC.interactionManager.attackEntity(MC.player, target);
        MC.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);

        if (!skipCooldown) attackCooldownTicks = getBaseCooldownTicks(stack, tps);

        long auraLogicDuration = System.nanoTime() - auraLogicStart;
        debugModule.debugAura(Text.of(String.format("logic time: %.3f ms", auraLogicDuration / 1_000_000.0)));
        long totalDuration = System.nanoTime() - startTime;
        debugModule.debugAura(Text.of(String.format("total %.3f ms", totalDuration / 1_000_000.0)));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPacketReceive(PacketReceiveEvent ev) {
        if (!(ev.getPacket() instanceof UpdateSelectedSlotC2SPacket)) return;
        if (MC.player == null || MC.world == null) return;

        MC.execute(() -> {
            ItemStack stack = MC.player.getMainHandStack();
            if (stack == null || stack.isEmpty()) return;

            float tps;
            switch (tpsMode.get()) {
                case LATEST -> tps = TICK_MANAGER.getLatestTPS();
                case AVERAGE -> tps = TICK_MANAGER.getAverageTPS();
                default -> tps = 20f;
            }

            attackCooldownTicks = getBaseCooldownTicks(stack, tps);
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (!render.get() || currentTarget == null) return;

        double eyeDist = getClosestEyeDistance(MC.player.getEyePos(), currentTarget.getBoundingBox());

        if (eyeDist > rotateRange.get()) return;

        ColorModule colorModule = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
        drawBox(currentTarget, colorModule.getStyledGlobalColor(), event.getMatrices(), event.getTickDelta());
    }

    private void drawBox(Entity entity, Color color, MatrixStack matrices, float partialTicks) {
        double interpX = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * partialTicks;
        double interpY = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * partialTicks;
        double interpZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * partialTicks;
        Box box = entity.getBoundingBox().offset(interpX - entity.getX(), interpY - entity.getY(), interpZ - entity.getZ());
        RenderUtil.drawBoxFilled(matrices, box, new Color(color.getRed(), color.getGreen(), color.getBlue(), 75));
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

    private static Vec3d getEntityCenter(Entity entity) {
        Box box = entity.getBoundingBox();
        double centerX = box.minX + (box.getLengthX() / 2);
        double centerY = box.minY + (box.getLengthY() / 2);
        double centerZ = box.minZ + (box.getLengthZ() / 2);
        return new Vec3d(centerX, centerY, centerZ);
    }

    private static double getClosestEyeDistance(Vec3d eyePos, Box box) {
        Vec3d closest;

        if (MC.player.isGliding()) {
            closest = box.getCenter();
        } else {
            closest = getClosestPointToEye(eyePos, box);
        }

        return eyePos.distanceTo(closest);
    }

    private static Vec3d getClosestPointToEye(Vec3d eyePos, Box box) {
        double x = eyePos.x;
        double y = eyePos.y;
        double z = eyePos.z;

        if (eyePos.x < box.minX) x = box.minX;
        else if (eyePos.x > box.maxX) x = box.maxX;

        if (eyePos.y < box.minY) y = box.minY;
        else if (eyePos.y > box.maxY) y = box.maxY;

        if (eyePos.z < box.minZ) z = box.minZ;
        else if (eyePos.z > box.maxZ) z = box.maxZ;

        return new Vec3d(x, y, z);
    }


    private EntityHitResult raycastTarget(Entity player, Entity target, double reach, float yaw, float pitch) {
        Vec3d eyePos = player.getCameraPosVec(1.0f);
        Vec3d look = getLookVectorFromYawPitch(yaw, pitch);
        Vec3d reachEnd = eyePos.add(look.multiply(vanillaRange.get() ? 3.00 : reach));

        Box targetBox = target.getBoundingBox();

        if (targetBox.raycast(eyePos, reachEnd).isPresent()) {
            return new EntityHitResult(target);
        }

        return null;
    }

    private static Vec3d getLookVectorFromYawPitch(float yaw, float pitch) {
        float fYaw = (float) Math.toRadians(yaw);
        float fPitch = (float) Math.toRadians(pitch);

        double x = -Math.cos(fPitch) * Math.sin(fYaw);
        double y = -Math.sin(fPitch);
        double z = Math.cos(fPitch) * Math.cos(fYaw);

        return new Vec3d(x, y, z).normalize();
    }

    private static float getBaseCooldownTicks(ItemStack stack, float tps) {
        float baseTicks;

        if (stack.isIn(ItemTags.SWORDS)) baseTicks = 12f;
        else if (stack.isIn(ItemTags.AXES)) baseTicks = 25f;
        else if (stack.getItem() instanceof TridentItem) baseTicks = 18f;
        else if (stack.getItem() instanceof MaceItem) baseTicks = 33f;
        else {
            float attackSpeed = 6f; // 2b2t allows from 4 to 6
            baseTicks = 20f / attackSpeed;
        }

        return baseTicks * (20f / tps);
    }
}
