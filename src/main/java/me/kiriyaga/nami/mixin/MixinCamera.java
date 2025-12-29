package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.visuals.FreeLookModule;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import me.kiriyaga.nami.feature.module.impl.visuals.ViewClipModule;
import me.kiriyaga.nami.mixininterface.ICamera;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(Camera.class)
public abstract class MixinCamera implements ICamera {

    @Shadow private float yRot;
    @Shadow private float xRot;

    @Unique
    private float tickDelta;

    @Shadow
    private static float DEFAULT_CAMERA_DISTANCE;

    @Override
    public void setRot(double yaw, double pitch) {
        this.yRot = (float) yaw;
        this.xRot = (float) pitch;
    }

    @Inject(method = "setup", at = @At("HEAD"))
    private void onUpdateHead(Level world, Entity entity, boolean bl, boolean bl2, float f, CallbackInfo ci) {
        this.tickDelta = tickDelta;
    }

    @ModifyVariable(method = "getMaxZoom", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float modifyClipToSpace(float d) {
        FreecamModule freecamModule = MODULE_MANAGER.getStorage() != null
                ? MODULE_MANAGER.getStorage().getByClass(FreecamModule.class)
                : null;

        if (freecamModule != null && freecamModule.isEnabled()) return 0;

        return d;
    }

    @ModifyArgs(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    private void onUpdateSetPosArgs(Args args) {
        FreecamModule freecamModule = MODULE_MANAGER.getStorage() != null
                ? MODULE_MANAGER.getStorage().getByClass(FreecamModule.class)
                : null;

        if (freecamModule != null && freecamModule.isEnabled()) {
            double x = freecamModule.prevPos.x + (freecamModule.pos.x - freecamModule.prevPos.x) * tickDelta;
            double y = freecamModule.prevPos.y + (freecamModule.pos.y - freecamModule.prevPos.y) * tickDelta;
            double z = freecamModule.prevPos.z + (freecamModule.pos.z - freecamModule.prevPos.z) * tickDelta;

            args.set(0, x);
            args.set(1, y);
            args.set(2, z);
        }
    }

    @ModifyArgs(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void onUpdateSetRotationArgs(Args args) {
        FreecamModule freecamModule = MODULE_MANAGER.getStorage() != null
                ? MODULE_MANAGER.getStorage().getByClass(FreecamModule.class)
                : null;
        FreeLookModule freeLookModule = MODULE_MANAGER.getStorage() != null
                ? MODULE_MANAGER.getStorage().getByClass(FreeLookModule.class)
                : null;

        if (freecamModule != null && freecamModule.isEnabled()) {
            float yaw = freecamModule.lastYaw + (freecamModule.yaw - freecamModule.lastYaw) * tickDelta;
            float pitch = freecamModule.lastPitch + (freecamModule.pitch - freecamModule.lastPitch) * tickDelta;

            args.set(0, yaw);
            args.set(1, pitch);
        } else if (freeLookModule != null && freeLookModule.isEnabled()) {
            args.set(0, freeLookModule.cameraYaw);
            args.set(1, freeLookModule.cameraPitch);
        }
    }

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void allowClip(float f, CallbackInfoReturnable<Float> i) {
        ViewClipModule viewClipModule = MODULE_MANAGER.getStorage() != null
                ? MODULE_MANAGER.getStorage().getByClass(ViewClipModule.class)
                : null;

        if (viewClipModule != null && viewClipModule.isEnabled()) {
            i.setReturnValue(f);
        }
    }

    @ModifyArgs(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private void extendDistance(Args args) {
        ViewClipModule viewClipModule = MODULE_MANAGER.getStorage() != null
                ? MODULE_MANAGER.getStorage().getByClass(ViewClipModule.class)
                : null;

        if (viewClipModule != null && viewClipModule.isEnabled()) {
            args.set(0, viewClipModule.getAnimatedDistance());
        }
    }
}
