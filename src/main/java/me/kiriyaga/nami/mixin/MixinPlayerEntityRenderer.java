package me.kiriyaga.nami.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.ROTATION_MANAGER;

@Mixin(PlayerEntityRenderer.class)
public abstract class MixinPlayerEntityRenderer {

    @Inject(method = "setupTransforms", at = @At("HEAD"))
    private void setupTransforms(PlayerEntityRenderState playerEntityRenderState, MatrixStack matrices, float f, float g, CallbackInfo ci) {
        if (shouldSpoofRotation()) {
            spoofRenderStateRotation(playerEntityRenderState);
        }
    }

    private boolean shouldSpoofRotation() {
        return ROTATION_MANAGER != null && ROTATION_MANAGER.getStateHandler() != null && ROTATION_MANAGER.getStateHandler().isRotating();
    }

    private void spoofRenderStateRotation(PlayerEntityRenderState renderState) {
        renderState.pitch = ROTATION_MANAGER.getStateHandler().getRotationPitch();
    }
}