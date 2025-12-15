package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.impl.visuals.FreeLookModule;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import me.kiriyaga.nami.feature.module.impl.visuals.NoWeatherModule;
import me.kiriyaga.nami.feature.module.impl.visuals.ViewClipModule;
import me.kiriyaga.nami.util.MatrixCache;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderTail(ObjectAllocator objectAllocator, RenderTickCounter tickCounter, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
        float tickDelta = tickCounter.getTickProgress(true);

        MatrixStack matrices = new MatrixStack();
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        EVENT_MANAGER.post(new Render3DEvent(
                matrices,
                tickCounter.getTickProgress(true),
                camera,
                matrix4f3,
                matrix4f
        ));

        matrices.pop();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void captureMatrices(ObjectAllocator objectAllocator, RenderTickCounter renderTickCounter, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
        MatrixCache.positionMatrix = new Matrix4f(matrix4f3);
        MatrixCache.projectionMatrix = new Matrix4f(matrix4f);
        MatrixCache.camera = camera;
    }
}
