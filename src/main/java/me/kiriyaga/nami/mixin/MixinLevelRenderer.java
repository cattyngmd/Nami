package me.kiriyaga.nami.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import me.kiriyaga.nami.feature.module.impl.visuals.NoWeatherModule;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.*;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRenderTail(GraphicsResourceAllocator objectAllocator, DeltaTracker tickCounter, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
        PoseStack matrices = new PoseStack();
        matrices.pushPose();
        matrices.mulPose(Axis.XP.rotationDegrees(camera.xRot()));
        matrices.mulPose(Axis.YP.rotationDegrees(camera.yRot() + 180.0F));

        EVENT_MANAGER.post(new Render3DEvent(matrices, tickCounter.getGameTimeDeltaPartialTick(true), camera, matrix4f3, matrix4f));

        matrices.popPose();
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void captureMatrices(GraphicsResourceAllocator objectAllocator, DeltaTracker renderTickCounter, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
        RenderUtil.PROJECTION_MATRIX.set(new Matrix4f(matrix4f2));
        //RenderUtil.MODEL_VIEW_MATRIX.set(new Matrix4f(matrix4f2));
        //  RenderUtil.POSITION_MATRIX.set(new Matrix4f(matrix4f3));
        RenderUtil.CAMERA = camera;
    }

    @Inject(method = "doesMobEffectBlockSky(Lnet/minecraft/client/Camera;)Z", at = @At("HEAD"), cancellable = true)
    private void doesMobEffectBlockSky(Camera camera, CallbackInfoReturnable<Boolean> cir) {
        NoRenderModule nr = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);

        if (nr != null && nr.isEnabled() && nr.noDarkness.get())
            cir.setReturnValue(false);
    }

    @Inject(method = "addWeatherPass", at = @At("HEAD"), cancellable = true)
    private void addWeatherPass(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
        NoWeatherModule nr = MODULE_MANAGER.getStorage().getByClass(NoWeatherModule.class);

        if (nr != null && nr.isEnabled())
            ci.cancel();
    }
}
