package me.kiriyaga.essentials.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.kiriyaga.essentials.feature.module.impl.render.NoRenderModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.essentials.Essentials.ROTATION_MANAGER;


@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    private boolean isTraveling = false;


    @Shadow
    public abstract void travel(Vec3d movementInput);


    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void travelPreHook(Vec3d movementInput, CallbackInfo ci) {
        if (isTraveling) return;
        if ((Object) this != MinecraftClient.getInstance().player || !ROTATION_MANAGER.isRotating()) return;

        if (movementInput.lengthSquared() < 1e-4) return;

        isTraveling = true;

        float spoofYaw = ROTATION_MANAGER.getRotationYaw();

        spoofYaw = findClosestValidYaw(spoofYaw);

        Vec3d fixedMovement = clampMovementToYaw(movementInput, spoofYaw);

        ci.cancel();
        this.travel(fixedMovement);

        float yaw = ((EntityAccessor) this).getYaw();
        ((EntityAccessor) this).setYaw(spoofYaw);

        isTraveling = false;
    }

    @ModifyExpressionValue(
            method = "jump",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"
            )
    )
    private float jumpFix(float originalYaw) {
        if ((Object)this != MinecraftClient.getInstance().player || !ROTATION_MANAGER.isRotating()) return originalYaw;

        return ROTATION_MANAGER.getRotationYaw();
    }

    private float findClosestValidYaw(float yaw) {
        float[] allowedYawAngles = new float[]{0, 45, 90, 135, 180, 225, 270, 315};
        float bestYaw = allowedYawAngles[0];
        float minDiff = Float.MAX_VALUE;
        for (float allowedYaw : allowedYawAngles) {
            float diff = Math.abs(((allowedYaw - yaw + 540f) % 360f) - 180f);
            if (diff < minDiff) {
                minDiff = diff;
                bestYaw = allowedYaw;
            }
        }
        return bestYaw;
    }

    private Vec3d clampMovementToYaw(Vec3d input, float yaw) {
        // Превращаем движение в локальные координаты игрока:
        double forward = input.z;
        double strafe = input.x;

        // Рассчитываем угол в радианах
        double yawRad = Math.toRadians(yaw);

        // Считаем глобальный вектор движения из локального и yaw
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);

        double motionX = strafe * cos - forward * sin;
        double motionZ = forward * cos + strafe * sin;

        return new Vec3d(motionX, input.y, motionZ);
    }

}
