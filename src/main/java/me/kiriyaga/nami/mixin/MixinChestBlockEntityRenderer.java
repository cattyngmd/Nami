package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.ChestBlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(ChestBlockEntityRenderer.class)
public abstract class MixinChestBlockEntityRenderer {

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRender(
            ChestBlockEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        NoRenderModule nr = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);
        if (nr != null && nr.isEnabled() && nr.tileEntity.get() >= 2) {


            Vec3d cameraPos = MC.getEntityRenderDispatcher().camera.getCameraPos();
            double distanceSquared = state.pos.getSquaredDistance(cameraPos);
            double maxDistance = Math.pow(nr.tileEntity.get(), 2);

            if (distanceSquared > maxDistance) ci.cancel();
        }
    }
}
