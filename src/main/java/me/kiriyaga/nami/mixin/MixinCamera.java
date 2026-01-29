package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.impl.feature.impl.visuals.FreeLookFeature;
import me.kiriyaga.nami.impl.feature.impl.visuals.FreecamFeature;
import me.kiriyaga.nami.impl.feature.impl.visuals.ViewClipFeature;
import me.kiriyaga.nami.mixininterface.ICamera;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
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

import static me.kiriyaga.nami.Nami.FEATURE_SERVICE;

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
        FreecamFeature freecamFeature = FEATURE_SERVICE.getStorage() != null
                ? FEATURE_SERVICE.getStorage().getByClass(FreecamFeature.class)
                : null;

        if (freecamFeature != null && freecamFeature.isEnabled()) return 0;

        return d;
    }

    @ModifyArgs(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    private void onUpdateSetPosArgs(Args args) {
        FreecamFeature freecamFeature = FEATURE_SERVICE.getStorage() != null
                ? FEATURE_SERVICE.getStorage().getByClass(FreecamFeature.class)
                : null;

        if (freecamFeature != null && freecamFeature.isEnabled()) {
            double x = freecamFeature.prevPos.x + (freecamFeature.pos.x - freecamFeature.prevPos.x) * tickDelta;
            double y = freecamFeature.prevPos.y + (freecamFeature.pos.y - freecamFeature.prevPos.y) * tickDelta;
            double z = freecamFeature.prevPos.z + (freecamFeature.pos.z - freecamFeature.prevPos.z) * tickDelta;

            args.set(0, x);
            args.set(1, y);
            args.set(2, z);
        }
    }

    @ModifyArgs(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void onUpdateSetRotationArgs(Args args) {
        FreecamFeature freecamFeature = FEATURE_SERVICE.getStorage() != null
                ? FEATURE_SERVICE.getStorage().getByClass(FreecamFeature.class)
                : null;
        FreeLookFeature freeLookFeature = FEATURE_SERVICE.getStorage() != null
                ? FEATURE_SERVICE.getStorage().getByClass(FreeLookFeature.class)
                : null;

        if (freecamFeature != null && freecamFeature.isEnabled()) {
            float yaw = freecamFeature.lastYaw + (freecamFeature.yaw - freecamFeature.lastYaw) * tickDelta;
            float pitch = freecamFeature.lastPitch + (freecamFeature.pitch - freecamFeature.lastPitch) * tickDelta;

            args.set(0, yaw);
            args.set(1, pitch);
        } else if (freeLookFeature != null && freeLookFeature.isEnabled()) {
            args.set(0, freeLookFeature.cameraYaw);
            args.set(1, freeLookFeature.cameraPitch);
        }
    }

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void allowClip(float f, CallbackInfoReturnable<Float> i) {
        ViewClipFeature viewClipFeature = FEATURE_SERVICE.getStorage() != null
                ? FEATURE_SERVICE.getStorage().getByClass(ViewClipFeature.class)
                : null;

        if (viewClipFeature != null && viewClipFeature.isEnabled()) {
            i.setReturnValue(f);
        }
    }

    @ModifyArgs(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private void extendDistance(Args args) {
        ViewClipFeature viewClipFeature = FEATURE_SERVICE.getStorage() != null
                ? FEATURE_SERVICE.getStorage().getByClass(ViewClipFeature.class)
                : null;

        if (viewClipFeature != null && viewClipFeature.isEnabled()) {
            args.set(0, viewClipFeature.getAnimatedDistance());
        }
    }
}
