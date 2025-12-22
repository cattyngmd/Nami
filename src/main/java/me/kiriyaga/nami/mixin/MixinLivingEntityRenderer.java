package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.client.RotationModule;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity> {

    @Inject(method = "updateRenderState", at = @At("HEAD")) // todo: fix this
    private void updateRenderState(T livingEntity, LivingEntityRenderState livingEntityRenderState, float f, CallbackInfo ci) {
        if (livingEntity instanceof ClientPlayerEntity player && player == MC.player && ROTATION_MANAGER.getStateHandler().isRotating() && MODULE_MANAGER.getStorage().getByClass(RotationModule.class).render.get()) {

            float prevYaw = livingEntityRenderState.bodyYaw + livingEntityRenderState.relativeHeadYaw;
            float prevPitch = livingEntityRenderState.pitch;
            float targetYaw = ROTATION_MANAGER.getStateHandler().getRotationYaw();
            float targetPitch = ROTATION_MANAGER.getStateHandler().getRotationPitch();
            float deltaYaw = targetYaw - prevYaw;
            if (deltaYaw > 180) deltaYaw -= 360;
            if (deltaYaw < -180) deltaYaw += 360;
            float smoothYaw = MathHelper.lerp(f, prevYaw, prevYaw + deltaYaw);
            float smoothPitch = MathHelper.lerp(f, prevPitch, targetPitch);

            livingEntityRenderState.relativeHeadYaw = MathHelper.wrapDegrees(smoothYaw - livingEntityRenderState.bodyYaw);
            livingEntityRenderState.pitch = smoothPitch;
        }
    }
}