package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.visuals.FreeLookModule;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import me.kiriyaga.nami.feature.module.impl.visuals.ViewClipModule;
import me.kiriyaga.nami.mixininterface.ICamera;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
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

    @Shadow private float yaw;
    @Shadow private float pitch;

    @Unique
    private float tickDelta;

    @Shadow
    private static float BASE_CAMERA_DISTANCE;

    @Override
    public void setRot(double yaw, double pitch) {
        this.yaw = (float) yaw;
        this.pitch = (float) pitch;
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void onUpdateHead(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        this.tickDelta = tickDelta;
    }

    @ModifyVariable(method = "clipToSpace", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float modifyClipToSpace(float d) {
        FreecamModule freecamModule = MODULE_MANAGER.getModule(FreecamModule.class);

        if (freecamModule.isEnabled()) return 0;

        return d;
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void onUpdateSetPosArgs(Args args) {
        FreecamModule freecamModule = MODULE_MANAGER.getModule(FreecamModule.class);

        if (freecamModule.isEnabled()) {
            double x = freecamModule.prevPos.x + (freecamModule.pos.x - freecamModule.prevPos.x) * tickDelta;
            double y = freecamModule.prevPos.y + (freecamModule.pos.y - freecamModule.prevPos.y) * tickDelta;
            double z = freecamModule.prevPos.z + (freecamModule.pos.z - freecamModule.prevPos.z) * tickDelta;

            args.set(0, x);
            args.set(1, y);
            args.set(2, z);
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void onUpdateSetRotationArgs(Args args) {
        FreecamModule freecamModule = MODULE_MANAGER.getModule(FreecamModule.class);
        FreeLookModule freeLookModule = MODULE_MANAGER.getModule(FreeLookModule.class);

        if (freecamModule.isEnabled()) {
            float yaw = freecamModule.lastYaw + (freecamModule.yaw - freecamModule.lastYaw) * tickDelta;
            float pitch = freecamModule.lastPitch + (freecamModule.pitch - freecamModule.lastPitch) * tickDelta;

            args.set(0, yaw);
            args.set(1, pitch);
        } else if (freeLookModule.isEnabled()) {
            args.set(0, freeLookModule.cameraYaw);
            args.set(1, freeLookModule.cameraPitch);
        }
    }

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void allowClip(float f, CallbackInfoReturnable<Float> i) {
        if (MODULE_MANAGER.getModule(ViewClipModule.class).isEnabled()) {
            i.setReturnValue(f);
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(F)F"))
    private void extendDistance(Args args) {
        if (MODULE_MANAGER.getModule(ViewClipModule.class).isEnabled()) {
            args.set(0, MODULE_MANAGER.getModule(ViewClipModule.class).distance.get().floatValue());
        }
    }
}
