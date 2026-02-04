package namidevelopment.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import namidevelopment.kiriyaga.nami.impl.feature.visuals.FreecamFeature;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngleState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static namidevelopment.kiriyaga.nami.Nami.MC;
import static namidevelopment.kiriyaga.api.NamiApi.FEATURE_SERVICE;

@Mixin(CompassAngleState.class)
public abstract class MixinCompassAngleState {

    @ModifyReturnValue(method = "getWrappedVisualRotationY(Lnet/minecraft/world/entity/ItemOwner;)F", at = @At("RETURN"))
    private static float getWrappedVisualRotationY(float original, ItemOwner context) {
        FreecamFeature freecam = FEATURE_SERVICE.getStorage().getByClass(FreecamFeature.class);
        if (freecam != null && freecam.isEnabled() && MC != null && MC.gameRenderer != null && MC.gameRenderer.getMainCamera() != null) {
            return MC.gameRenderer.getMainCamera().yRot() / 360.0F;
        }

        return original;
    }

    @ModifyReturnValue(method = "getAngleFromEntityToPos(Lnet/minecraft/world/entity/ItemOwner;Lnet/minecraft/core/BlockPos;)D", at = @At("RETURN"))
    private static double getAngleFromEntityToPos(double original, ItemOwner context, BlockPos pos) {
        FreecamFeature freecam = FEATURE_SERVICE.getStorage().getByClass(FreecamFeature.class);
        if (freecam != null && freecam.isEnabled() && MC != null && MC.gameRenderer != null && MC.gameRenderer.getMainCamera() != null) {
            Camera camera = MC.gameRenderer.getMainCamera();
            Vec3 target = Vec3.atCenterOf(pos);
            Vec3 camPos = camera.position();

            return Math.atan2(target.z - camPos.z, target.x - camPos.x) / (Math.PI * 2.0);
        }

        return original;
    }
}
