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

    private float originalYaw, originalBodyYaw, originalHeadYaw;
    private float originalPitch;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    private void applySpoofRotation() {
        float spoofYaw = ROTATION_MANAGER.getRotationYaw();
        float spoofPitch = ROTATION_MANAGER.getRotationPitch();

        this.setYaw(spoofYaw);
        this.setPitch(spoofPitch);
        ((LivingEntityAccessor) this).setBodyYaw(spoofYaw);
        ((LivingEntityAccessor) this).setHeadYaw(spoofYaw);
    }

    private void restoreOriginalRotation() {
        this.setYaw(originalYaw);
        this.setPitch(originalPitch);
        ((LivingEntityAccessor) this).setBodyYaw(originalBodyYaw);
        ((LivingEntityAccessor) this).setHeadYaw(originalHeadYaw);
    }

    private void saveOriginalRotation() {
        originalYaw = this.getYaw();
        originalPitch = this.getPitch();
        originalBodyYaw = ((LivingEntityAccessor) this).getBodyYaw();
        originalHeadYaw = ((LivingEntityAccessor) this).getHeadYaw();
    }

    @Inject(method = "spawnItemParticles", at = @At("HEAD"), cancellable = true)
    private void spawnItemParticles(ItemStack stack, int count, CallbackInfo info) {
        NoRenderModule noRender = MODULE_MANAGER.getModule(NoRenderModule.class);
        if (noRender.isEnabled() && noRender.isNoEating() && stack.getComponents().contains(DataComponentTypes.FOOD)) info.cancel();
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void onTravelPre(Vec3d movementInput, CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;
        saveOriginalRotation();
        applySpoofRotation();
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void onTravelPost(Vec3d movementInput, CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;
        restoreOriginalRotation();
    }

    @Inject(method = "jump", at = @At("HEAD"))
    private void onJumpPre(CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;
        saveOriginalRotation();
        applySpoofRotation();
    }

    @Inject(method = "jump", at = @At("TAIL"))
    private void onJumpPost(CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;
        restoreOriginalRotation();
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovementPre(CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;
        saveOriginalRotation();
        applySpoofRotation();
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void onTickMovementPost(CallbackInfo ci) {
        if ((Object)this != MinecraftClient.getInstance().player) return;
        restoreOriginalRotation();
    }
}
