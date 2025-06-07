package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.event.impl.Render3DEvent;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.essentials.Essentials.*;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderTail(
            ObjectAllocator allocator,
            RenderTickCounter tickCounter,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            Matrix4f positionMatrix,
            Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        MatrixStack matrices = new MatrixStack();
        matrices.push();

        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        float tickDelta = tickCounter.getTickProgress(true);

        EVENT_MANAGER.post(new Render3DEvent(matrices, tickDelta));

        matrices.pop();
    }
}
