package me.kiriyaga.essentials.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
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

    @Inject(method = "travel", at = @At("HEAD"))
    private void travelPreHook(Vec3d movementInput, CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;

        spoofing = true;

        originalYaw = this.getYaw();
        originalBodyYaw = ((LivingEntityAccessor) this).getBodyYaw();
        originalHeadYaw = ((LivingEntityAccessor) this).getHeadYaw();

        float spoofYaw = ROTATION_MANAGER.getRotationYaw();
        this.setYaw(spoofYaw);
        ((LivingEntityAccessor) this).setBodyYaw(spoofYaw);
        ((LivingEntityAccessor) this).setHeadYaw(spoofYaw);
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void travelPostHook(Vec3d movementInput, CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;

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
        if ((Object)this != MinecraftClient.getInstance().player) return originalYaw;

        return ROTATION_MANAGER.getRotationYaw();
    }
}