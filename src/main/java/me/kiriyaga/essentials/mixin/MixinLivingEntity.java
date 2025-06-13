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

    private float originalYaw, originalBodyYaw, originalHeadYaw;
    private boolean spoofing = false;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void travelPreHook(Vec3d movementInput, CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player || !ROTATION_MANAGER.isRotating()) return;

        if (movementInput.x == 0 && movementInput.z == 0) return;

        float visualYaw = this.getYaw();
        float spoofYaw = findClosestValidYaw(visualYaw, movementInput);

        spoofing = true;
        originalYaw = this.getYaw();
        originalBodyYaw = ((LivingEntityAccessor) this).getBodyYaw();
        originalHeadYaw = ((LivingEntityAccessor) this).getHeadYaw();

        this.setYaw(spoofYaw);
        ((LivingEntityAccessor) this).setBodyYaw(spoofYaw);
        ((LivingEntityAccessor) this).setHeadYaw(spoofYaw);
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void travelPostHook(Vec3d movementInput, CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player || !ROTATION_MANAGER.isRotating()) return;

        spoofing = false;

        this.setYaw(originalYaw);
        ((LivingEntityAccessor) this).setBodyYaw(originalBodyYaw);
        ((LivingEntityAccessor) this).setHeadYaw(originalHeadYaw);
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

    private float findClosestValidYaw(float visualYaw, Vec3d movementInput) {
        double angleRad = Math.atan2(-movementInput.x, movementInput.z);
        float movementYaw = (float) Math.toDegrees(angleRad);
        movementYaw = (movementYaw + 360f) % 360f;

        float[] allowedYawAngles = new float[] {
                0, 45, 90, 135, 180, 225, 270, 315
        };

        float bestYaw = allowedYawAngles[0];
        float minDiff = Float.MAX_VALUE;

        for (float yaw : allowedYawAngles) {
            float diff = Math.abs(((yaw - visualYaw + 540f) % 360f) - 180f);
            if (diff < minDiff) {
                minDiff = diff;
                bestYaw = yaw;
            }
        }

        return bestYaw;
    }
}
