package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngleState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(CompassAngleState.class)
public abstract class MixinCompassAngleState {

    @ModifyReturnValue(
            method = "getWrappedVisualRotationY(Lnet/minecraft/world/entity/ItemOwner;)F",
            at = @At("RETURN")
    )
    private static float nami$overrideBodyYaw(float original, ItemOwner context) {
        FreecamModule freecam = MODULE_MANAGER.getStorage().getByClass(FreecamModule.class);
        if (freecam != null && freecam.isEnabled()
                && MC != null
                && MC.gameRenderer != null
                && MC.gameRenderer.getMainCamera() != null) {

            return MC.gameRenderer.getMainCamera().yRot() / 360.0F;
        }

        return original;
    }

    @ModifyReturnValue(
            method = "getAngleFromEntityToPos(Lnet/minecraft/world/entity/ItemOwner;Lnet/minecraft/core/BlockPos;)D",
            at = @At("RETURN")
    )
    private static double nami$overrideAngleTo(double original, ItemOwner context, BlockPos pos) {
        FreecamModule freecam = MODULE_MANAGER.getStorage().getByClass(FreecamModule.class);
        if (freecam != null && freecam.isEnabled()
                && MC != null
                && MC.gameRenderer != null
                && MC.gameRenderer.getMainCamera() != null) {

            Camera camera = MC.gameRenderer.getMainCamera();
            Vec3 target = Vec3.atCenterOf(pos);
            Vec3 camPos = camera.position();

            return Math.atan2(
                    target.z - camPos.z,
                    target.x - camPos.x
            ) / (Math.PI * 2.0);
        }

        return original;
    }
}
