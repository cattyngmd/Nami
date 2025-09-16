package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.DebugModule;
import me.kiriyaga.nami.feature.module.impl.movement.SprintModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
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
import static me.kiriyaga.nami.util.RotationUtils.*;

@RegisterModule
public class AuraModule extends Module {

    public enum TpsMode { NONE, LATEST, AVERAGE }
    public enum Rotate { NORMAL, HOLD}

    public final DoubleSetting attackRange = addSetting(new DoubleSetting("range", 3.00, 1.0, 6.0));
    public final BoolSetting vanillaRange = addSetting(new BoolSetting("vanilla range", true));
    public final BoolSetting swordOnly = addSetting(new BoolSetting("weap only", false));
    public final EnumSetting<TpsMode> tpsMode = addSetting(new EnumSetting<>("tps", TpsMode.NONE));
    public final BoolSetting multiTask = addSetting(new BoolSetting("multitask", false)); // TODO: fix this it resets eating
    public final BoolSetting stopSprinting = addSetting(new BoolSetting("stop sprinting", true));
    public final BoolSetting raycast = addSetting(new BoolSetting("raycast", true));
    public final BoolSetting raycastConfirm = addSetting(new BoolSetting("raycast confirm", true));
    public final EnumSetting<Rotate> rotate = addSetting(new EnumSetting<>("rotate", Rotate.NORMAL));
    public final BoolSetting render = addSetting(new BoolSetting("render", true));

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
        DebugModule debugModule = MODULE_MANAGER.getStorage().getByClass(DebugModule.class);

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

        double preRotate;

        switch (rotate.get()) { // yes
            case NORMAL -> preRotate = 0.10;
            case HOLD -> preRotate = 1.00;
            default -> preRotate = 0.10;
        }

        if (eyeDist <= attackRange.get()+0.10 && (skipCooldown || attackCooldownTicks <= preRotate * tps)) {
            Vec3d rotationTarget;
            if (raycast.get()) {
                Vec3d eyePos = MC.player.getCameraPosVec(1.0f);
                rotationTarget = getClosestPointToEye(eyePos, target.getBoundingBox());
            } else {
                rotationTarget = getEntityCenter(target);
            }

            ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(
                    AuraModule.class.getName(),
                    5,
                    (float) getYawToVec(MC.player, rotationTarget),
                    (float) getPitchToVec(MC.player, rotationTarget)
            ));

            SprintModule m = MODULE_MANAGER.getStorage().getByClass(SprintModule.class);
            // This one done in rotation since its the most easy and stable as i see now, somehow people also 0-tick them but it doesnt for for us, and its either flags grim or doesnt work properly
            // TODO: 1 tick them instead of rotation
            if (stopSprinting.get() && m != null && m.isEnabled())
                m.stopSprinting(1);
        }

        if (!ROTATION_MANAGER.getRequestHandler().isCompleted(AuraModule.class.getName()) && (!raycast.get() || !raycastConfirm.get()))
            return;

        boolean canAttack;
        if (raycast.get()) {
            EntityHitResult attackHit;
            if (raycastConfirm.get()) {
                attackHit = raycastTarget(MC.player, target, attackRange.get(),
                        ROTATION_MANAGER.getStateHandler().getServerYaw(),
                        ROTATION_MANAGER.getStateHandler().getServerPitch());
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

        if (eyeDist > attackRange.get()+0.10) return;

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

    private static float getBaseCooldownTicks(ItemStack stack, float tps) {
        float baseTicks;

        if (stack.isIn(ItemTags.SWORDS)) baseTicks = 12f;
        else if (stack.isIn(ItemTags.AXES)) baseTicks = 20f;
        else if (stack.getItem() instanceof TridentItem) baseTicks = 18f;
        else if (stack.getItem() instanceof MaceItem) baseTicks = 33f;
        else {
            float attackSpeed = 6f; // 2b2t allows from 4 to 6
            baseTicks = 20f / attackSpeed;
        }

        return baseTicks * (20f / tps);
    }
}
