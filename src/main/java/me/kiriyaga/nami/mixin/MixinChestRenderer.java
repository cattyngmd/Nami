package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.impl.feature.impl.visuals.NoRenderFeature;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.FEATURE_SERVICE;

@Mixin(ChestRenderer.class)
public abstract class MixinChestRenderer {

    @Inject(
            method = "submit",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRender(
            ChestRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        NoRenderFeature nr = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (nr != null && nr.isEnabled() && nr.tileEntity.get() >= 2) {


            Vec3 cameraPos = MC.getEntityRenderDispatcher().camera.position();
            double distanceSquared = state.blockPos.distToCenterSqr(cameraPos);
            double maxDistance = Math.pow(nr.tileEntity.get(), 2);

            if (distanceSquared > maxDistance) ci.cancel();
        }
    }
}
