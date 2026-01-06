package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.kiriyaga.nami.event.impl.GlidingEvent;
import me.kiriyaga.nami.feature.module.impl.client.RotationsModule;
import me.kiriyaga.nami.feature.module.impl.movement.HighJumpModule;
import me.kiriyaga.nami.feature.module.impl.exploits.NoJumpDelayModule;
import me.kiriyaga.nami.feature.module.impl.movement.NoLevitationModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.*;


@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    private float originalYaw;
    @Shadow
    private int noJumpDelay;
    @Shadow
    public float yHeadRot;
    private float originalPitch;

    public MixinLivingEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void travelPreHook(Vec3 movementInput, CallbackInfo ci) {
        if (Minecraft.getInstance() == null || Minecraft.getInstance().player != (Object)this) return;
        if (MODULE_MANAGER.getStorage() == null) return;
        RotationsModule rotationsModule = MODULE_MANAGER.getStorage().getByClass(RotationsModule.class);
        if (rotationsModule == null || !rotationsModule.moveFix.get()) return;
        if (ROTATION_MANAGER == null || !ROTATION_MANAGER.getStateHandler().isRotating()) return;

        originalYaw = super.getYRot();
        originalPitch = super.getXRot();

        float spoofYaw = ROTATION_MANAGER.getStateHandler().getRotationYaw();
        float spoofPitch = ROTATION_MANAGER.getStateHandler().getRotationPitch();

        this.setYRot(spoofYaw);
        this.setXRot(spoofPitch);
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void travelPostHook(Vec3 movementInput, CallbackInfo ci) {
        if (Minecraft.getInstance() == null || Minecraft.getInstance().player != (Object)this) return;
        if (ROTATION_MANAGER == null || !ROTATION_MANAGER.getStateHandler().isRotating()) return;

        if (MODULE_MANAGER.getStorage() == null) return;
        RotationsModule rotationsModule = MODULE_MANAGER.getStorage().getByClass(RotationsModule.class);
        if (rotationsModule == null || !rotationsModule.moveFix.get()) return;

        this.setYRot(originalYaw);
        this.setXRot(originalPitch);
    }

    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"))
    private float jumpFix(float originalYaw) {
        if ((Object)this != Minecraft.getInstance().player) return originalYaw;
        return ROTATION_MANAGER.getStateHandler().isRotating() ? ROTATION_MANAGER.getStateHandler().getRotationYaw() : originalYaw;
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 2, shift = At.Shift.BEFORE))
    private void doItemUse(CallbackInfo info) {
        NoJumpDelayModule module = MODULE_MANAGER.getStorage() != null ? MODULE_MANAGER.getStorage().getByClass(NoJumpDelayModule.class) : null;
        if (module != null && module.isEnabled()) {
            noJumpDelay = 0;
        }
    }

    @Inject(at = @At("HEAD"), method = "isFallFlying()Z", cancellable = true)
    private void isGlidingZ(CallbackInfoReturnable<Boolean> cir) {
        GlidingEvent ev = new GlidingEvent();

        EVENT_MANAGER.post(ev);

        if (ev.isCancelled())
            cir.setReturnValue(true);
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        HighJumpModule mod = MODULE_MANAGER.getStorage() != null ? MODULE_MANAGER.getStorage().getByClass(HighJumpModule.class) : null;
        if (mod == null || !mod.isEnabled()) return;

        float jumpBoost = mod.height.get().floatValue();

        Vec3 vel = this.getDeltaMovement();
        this.setDeltaMovement(vel.x, Math.max(jumpBoost, vel.y), vel.z);

        if (this.isSprinting()) {
            float yawRad = this.getYRot() * 0.017453292F;
            this.addDeltaMovement(new Vec3(-Mth.sin(yawRad) * 0.2, 0.0, Mth.cos(yawRad) * 0.2));
        }

        this.needsSync = true;
        ci.cancel();
    }

//    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
//    private void setSprinting(boolean sprinting, CallbackInfo ci) {
//        if ((Object)this != MinecraftClient.getInstance().player) return;
//
//        RotationManagerModule rotationModule = MODULE_MANAGER.getStorage() != null ? MODULE_MANAGER.getStorage().getByClass(RotationManagerModule.class) : null;
//        if (rotationModule == null || ROTATION_MANAGER == null || !ROTATION_MANAGER.getStateHandler().isRotating() || !rotationModule.sprintFix.get())
//            return;
//
//        if (sprinting && MC.player.input != null) {
//            Vec2f movement = MC.player.input.getMovementInput();
//            float forward = movement.x;
//            float sideways = movement.y;
//
//            if (forward == 0 && sideways == 0) {
//                ci.cancel();
//                super.setSprinting(false);
//                return;
//            }
//
//            float spoofYaw = lastSendedYaw;
//            float realYaw = MC.player.getYaw();
//
//            Vec3d localMovement = new Vec3d(sideways, 0, forward);
//            Vec3d globalMovement = localToGlobal(localMovement, realYaw);
//
//            Vec3d localRelativeToSpoof = globalToLocal(globalMovement, spoofYaw);
//
//            double moveAngleRad = Math.atan2(localRelativeToSpoof.z, localRelativeToSpoof.x);
//            float moveAngleDeg = (float) Math.toDegrees(moveAngleRad);
//            moveAngleDeg = MathHelper.wrapDegrees(moveAngleDeg);
//
//            if (Math.abs(moveAngleDeg) > 33f) {
//                ci.cancel();
//                super.setSprinting(false);
//            }
//        }
//    }

    @ModifyReturnValue(method = "hasEffect", at = @At("RETURN"))
    private boolean hasStatusEffect(boolean original, Holder<MobEffect> effect) {
        if ((Object) this instanceof Player player &&
                player == MC.player) {

            NoLevitationModule nl = MODULE_MANAGER.getStorage().getByClass(NoLevitationModule.class);
            if (nl != null && nl.isEnabled()) {
                ResourceKey<MobEffect> slowFallKey = MobEffects.SLOW_FALLING.unwrapKey().orElse(null);
                if (nl.noSlowFall.get() && slowFallKey != null && effect.is(slowFallKey))
                    return false;

                ResourceKey<MobEffect> levitationKey = MobEffects.LEVITATION.unwrapKey().orElse(null); // this just removes levitation damage reducing
                if (levitationKey != null && effect.is(levitationKey))
                    return false;
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "getEffect", at = @At("RETURN"))
    private MobEffectInstance getStatusEffect(MobEffectInstance original, Holder<MobEffect> effect) {
        NoLevitationModule nl = MODULE_MANAGER.getStorage().getByClass(NoLevitationModule.class);
        if (nl != null && nl.isEnabled()) {
            if (effect == MobEffects.LEVITATION)
                return null;
//            if (nl.noSlowFall.get() && effect.value == StatusEffects.SLOW_FALLING)
//                return null;
        }
        return original;
    }

    @ModifyVariable(method = "tickHeadTurn(F)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float turnHead(float f) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (self instanceof LocalPlayer player && player == MC.player && ROTATION_MANAGER.getStateHandler().isRotating() && MODULE_MANAGER.getStorage().getByClass(RotationsModule.class).render.get()) {
            return ROTATION_MANAGER.getStateHandler().getRotationYaw();
        }
        return f;
    }

    @Inject(method = "lerpHeadRotationStep", at = @At("HEAD"), cancellable = true)
    private void lerpHeadYawInject(int i, double d, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (self instanceof LocalPlayer player && player == MC.player && ROTATION_MANAGER.getStateHandler().isRotating() && MODULE_MANAGER.getStorage().getByClass(RotationsModule.class).render.get()) {

            double targetYaw = ROTATION_MANAGER.getStateHandler().getRotationYaw();

            this.yHeadRot = (float) Mth.rotLerp(1.0 / i, this.yHeadRot, targetYaw);

            ci.cancel();
        }
    }
}
