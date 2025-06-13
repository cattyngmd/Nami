package me.kiriyaga.essentials.mixin;

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

import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;
import static me.kiriyaga.essentials.Essentials.ROTATION_MANAGER;


@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    private float travelOriginalYaw, travelOriginalBodyYaw, travelOriginalHeadYaw;

    private float jumpOriginalYaw, jumpOriginalBodyYaw, jumpOriginalHeadYaw;

    private float tickOriginalYaw, tickOriginalBodyYaw, tickOriginalHeadYaw;
    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    private void saveOriginalRotationForTravel() {
        travelOriginalYaw = this.getYaw();
        travelOriginalBodyYaw = ((LivingEntityAccessor) this).getBodyYaw();
        travelOriginalHeadYaw = ((LivingEntityAccessor) this).getHeadYaw();
    }

    private void restoreOriginalRotationForTravel() {
        this.setYaw(travelOriginalYaw);
        ((LivingEntityAccessor) this).setBodyYaw(travelOriginalBodyYaw);
        ((LivingEntityAccessor) this).setHeadYaw(travelOriginalHeadYaw);
    }

    private void saveOriginalRotationForJump() {
        jumpOriginalYaw = this.getYaw();
        jumpOriginalBodyYaw = ((LivingEntityAccessor) this).getBodyYaw();
        jumpOriginalHeadYaw = ((LivingEntityAccessor) this).getHeadYaw();
    }

    private void restoreOriginalRotationForJump() {
        this.setYaw(jumpOriginalYaw);
        ((LivingEntityAccessor) this).setBodyYaw(jumpOriginalBodyYaw);
        ((LivingEntityAccessor) this).setHeadYaw(jumpOriginalHeadYaw);
    }

    private void saveOriginalRotationForTick() {
        tickOriginalYaw = this.getYaw();
        tickOriginalBodyYaw = ((LivingEntityAccessor) this).getBodyYaw();
        tickOriginalHeadYaw = ((LivingEntityAccessor) this).getHeadYaw();
    }

    private void restoreOriginalRotationForTick() {
        this.setYaw(tickOriginalYaw);
        ((LivingEntityAccessor) this).setBodyYaw(tickOriginalBodyYaw);
        ((LivingEntityAccessor) this).setHeadYaw(tickOriginalHeadYaw);
    }

    private void applySpoofRotation() {
        float spoofYaw = ROTATION_MANAGER.getRotationYaw();

        this.setYaw(spoofYaw);
        ((LivingEntityAccessor) this).setBodyYaw(spoofYaw);
        ((LivingEntityAccessor) this).setHeadYaw(spoofYaw);
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void onTravelPre(Vec3d movementInput, CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;

        saveOriginalRotationForTravel();
        applySpoofRotation();
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void onTravelPost(Vec3d movementInput, CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;

        restoreOriginalRotationForTravel();
    }

    @Inject(method = "jump", at = @At("HEAD"))
    private void onJumpPre(CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;

        saveOriginalRotationForJump();
        applySpoofRotation();
    }

    @Inject(method = "jump", at = @At("TAIL"))
    private void onJumpPost(CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;

        restoreOriginalRotationForJump();
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovementPre(CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;

        saveOriginalRotationForTick();
        applySpoofRotation();
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void onTickMovementPost(CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;

        restoreOriginalRotationForTick();
    }
}
