package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.item.property.numeric.CompassState;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(CompassState.class)
public abstract class MixinCompassState {

    @ModifyReturnValue(
            method = "getBodyYaw(Lnet/minecraft/util/HeldItemContext;)F",
            at = @At("RETURN")
    )
    private static float nami$overrideBodyYaw(float original, HeldItemContext context) {
        FreecamModule freecam = MODULE_MANAGER.getStorage().getByClass(FreecamModule.class);
        if (freecam != null && freecam.isEnabled()
                && MC != null
                && MC.gameRenderer != null
                && MC.gameRenderer.getCamera() != null) {

            return MC.gameRenderer.getCamera().getYaw() / 360.0F;
        }

        return original;
    }

    @ModifyReturnValue(
            method = "getAngleTo(Lnet/minecraft/util/HeldItemContext;Lnet/minecraft/util/math/BlockPos;)D",
            at = @At("RETURN")
    )
    private static double nami$overrideAngleTo(double original, HeldItemContext context, BlockPos pos) {
        FreecamModule freecam = MODULE_MANAGER.getStorage().getByClass(FreecamModule.class);
        if (freecam != null && freecam.isEnabled()
                && MC != null
                && MC.gameRenderer != null
                && MC.gameRenderer.getCamera() != null) {

            Camera camera = MC.gameRenderer.getCamera();
            Vec3d target = Vec3d.ofCenter(pos);
            Vec3d camPos = camera.getCameraPos();

            return Math.atan2(
                    target.z - camPos.z,
                    target.x - camPos.x
            ) / (Math.PI * 2.0);
        }

        return original;
    }
}
