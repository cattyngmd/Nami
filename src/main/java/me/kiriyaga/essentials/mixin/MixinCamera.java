package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.feature.module.impl.render.FreeLookModule;
import me.kiriyaga.essentials.mixininterface.ICamera;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

@Mixin(Camera.class)
public abstract class MixinCamera implements ICamera {

    @Shadow private float yaw;
    @Shadow private float pitch;

    @Shadow
    private static float BASE_CAMERA_DISTANCE;

    @Override
    public void setRot(double yaw, double pitch) {
        this.yaw = (float) yaw;
        this.pitch = (float) pitch;
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void onUpdateSetRotationArgs(Args args) {
        FreeLookModule freeLookModule = MODULE_MANAGER.getModule(FreeLookModule.class);
        if (freeLookModule != null && freeLookModule.isEnabled()) {
            args.set(0, freeLookModule.cameraYaw);
            args.set(1, freeLookModule.cameraPitch);
        }
    }
}
