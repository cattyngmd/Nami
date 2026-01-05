package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.client.RotationsModule;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity> {

    @Inject(method = "extractRenderState", at = @At("HEAD")) // todo: fix this
    private void updateRenderState(T livingEntity, LivingEntityRenderState livingEntityRenderState, float f, CallbackInfo ci) {
        if (livingEntity instanceof LocalPlayer player && player == MC.player && ROTATION_MANAGER.getStateHandler().isRotating() && MODULE_MANAGER.getStorage().getByClass(RotationsModule.class).render.get()) {

            float prevYaw = livingEntityRenderState.bodyRot + livingEntityRenderState.yRot;
            float prevPitch = livingEntityRenderState.xRot;
            float targetYaw = ROTATION_MANAGER.getStateHandler().getRotationYaw();
            float targetPitch = ROTATION_MANAGER.getStateHandler().getRotationPitch();
            float deltaYaw = targetYaw - prevYaw;
            if (deltaYaw > 180) deltaYaw -= 360;
            if (deltaYaw < -180) deltaYaw += 360;
            float smoothYaw = Mth.lerp(f, prevYaw, prevYaw + deltaYaw);
            float smoothPitch = Mth.lerp(f, prevPitch, targetPitch);

            livingEntityRenderState.yRot = Mth.wrapDegrees(smoothYaw - livingEntityRenderState.bodyRot);
            livingEntityRenderState.xRot = smoothPitch;
        }
    }
}