package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.api.event.impl.EntityPushEvent;
import namidevelopment.kiriyaga.nami.impl.feature.visuals.ESPFeature;
import namidevelopment.kiriyaga.nami.impl.feature.visuals.FreeLookFeature;
import namidevelopment.kiriyaga.nami.impl.feature.visuals.FreecamFeature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;
@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow public abstract float getYRot();

    @Shadow public abstract float getXRot();


    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;

        Color espColor = ESPFeature.getESPColor(self);
        if (espColor != null) {
            cir.setReturnValue(espColor.getRGB() & 0xFFFFFF);
        }
    }

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void updateChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if ((Object) this != MC.player) return;

        FreecamFeature freecamFeature = FEATURE_SERVICE.getStorage().getByClass(FreecamFeature.class);
        FreeLookFeature freeLookFeature = FEATURE_SERVICE.getStorage().getByClass(FreeLookFeature.class);

        if (freecamFeature != null && freecamFeature.isEnabled()) {
            freecamFeature.changeLookDirection(cursorDeltaX * 0.15, cursorDeltaY * 0.15);
            ci.cancel();
        } else if (freeLookFeature != null && freeLookFeature.isEnabled()) {
            freeLookFeature.cameraYaw += (float) (cursorDeltaX / freeLookFeature.sensivity.get().floatValue());
            freeLookFeature.cameraPitch += (float) (cursorDeltaY / freeLookFeature.sensivity.get().floatValue());

            if (Math.abs(freeLookFeature.cameraPitch) > 90.0F)
                freeLookFeature.cameraPitch = freeLookFeature.cameraPitch > 0.0F ? 90.0F : -90.0F;

            ci.cancel();
        }
    }

    @Inject(method = "push", at = @At(value = "HEAD"), cancellable = true)
    private void pushAwayFrom(Entity e, CallbackInfo ci) {
        EntityPushEvent pushEntityEvent = new EntityPushEvent((Entity)(Object) this, e);
        EVENT_SERVICE.post(pushEntityEvent);
        if (pushEntityEvent.isCancelled()) ci.cancel();
    }

    // this is like not needed?
/*    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;getPose()Lnet/minecraft/entity/EntityPose;", cancellable = true)
    private void entityPose(CallbackInfoReturnable<EntityPose> cir) {
        ElytraFlyFeature elytraFlyFeature = Feature_SERVICE.getStorage().getByClass(ElytraFlyFeature.class);
        if (elytraFlyFeature != null && elytraFlyFeature.isEnabled()
                && elytraFlyFeature.mode.get() == ElytraFlyFeature.FlyMode.BOUNCE
                && (Object) this == MinecraftClient.getInstance().player
                && MC.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            cir.setReturnValue(EntityPose.STANDING);
        }
    }*/

    @Inject(method = "getLookAngle()Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void onGetRotationVector(CallbackInfoReturnable<Vec3> cir) {
        if ((Object) this != MC.player) return;
        if (ROTATION_SERVICE == null || !ROTATION_SERVICE.getStateHandler().isRotating()) return;

        float spoofYaw = ROTATION_SERVICE.getStateHandler().getRotationYaw();
        float spoofPitch = ROTATION_SERVICE.getStateHandler().getRotationPitch();

        cir.setReturnValue(((Entity) (Object) this).calculateViewVector(spoofPitch, spoofYaw));
    }

/*    @Inject(method = "lookAt", at = @At("TAIL"))
    private void onLookAt(EntityAnchorArgument.Anchor anchor, Vec3 vec3, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;

        if (!(entity instanceof LocalPlayer player)) return;
        if (player != Minecraft.getInstance().player) return;

        RotationsFeature rotations = Feature_SERVICE.getStorage().getByClass(RotationsFeature.class);
        if (!rotations.render.get()) return;
        RotationStateHandler handler = ROTATION_SERVICE.getStateHandler();
        handler.setRenderPitch(player.getXRot());
        handler.setRenderHeadYaw(player.yHeadRot);
        handler.setRenderBodyYaw(player.yBodyRot);
    }*/
}
