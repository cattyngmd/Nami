package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.core.rotation.RotationStateHandler;
import me.kiriyaga.nami.event.impl.EntityPushEvent;
import me.kiriyaga.nami.feature.module.impl.client.RotationsModule;
import me.kiriyaga.nami.feature.module.impl.movement.ElytraFlyModule;
import me.kiriyaga.nami.feature.module.impl.visuals.ESPModule;
import me.kiriyaga.nami.feature.module.impl.visuals.FreeLookModule;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow public abstract float getYRot();

    @Shadow public abstract float getXRot();


    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;

        Color espColor = ESPModule.getESPColor(self);
        if (espColor != null) {
            cir.setReturnValue(espColor.getRGB() & 0xFFFFFF);
        }
    }

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void updateChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if ((Object) this != MC.player) return;

        FreecamModule freecamModule = MODULE_MANAGER.getStorage().getByClass(FreecamModule.class);
        FreeLookModule freeLookModule = MODULE_MANAGER.getStorage().getByClass(FreeLookModule.class);

        if (freecamModule != null && freecamModule.isEnabled()) {
            freecamModule.changeLookDirection(cursorDeltaX * 0.15, cursorDeltaY * 0.15);
            ci.cancel();
        } else if (freeLookModule != null && freeLookModule.isEnabled()) {
            freeLookModule.cameraYaw += (float) (cursorDeltaX / freeLookModule.sensivity.get().floatValue());
            freeLookModule.cameraPitch += (float) (cursorDeltaY / freeLookModule.sensivity.get().floatValue());

            if (Math.abs(freeLookModule.cameraPitch) > 90.0F)
                freeLookModule.cameraPitch = freeLookModule.cameraPitch > 0.0F ? 90.0F : -90.0F;

            ci.cancel();
        }
    }

    @Inject(method = "push", at = @At(value = "HEAD"), cancellable = true)
    private void pushAwayFrom(Entity e, CallbackInfo ci) {
        EntityPushEvent pushEntityEvent = new EntityPushEvent((Entity)(Object) this, e);
        EVENT_MANAGER.post(pushEntityEvent);
        if (pushEntityEvent.isCancelled()) ci.cancel();
    }

    // this is like not needed?
/*    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;getPose()Lnet/minecraft/entity/EntityPose;", cancellable = true)
    private void entityPose(CallbackInfoReturnable<EntityPose> cir) {
        ElytraFlyModule elytraFlyModule = MODULE_MANAGER.getStorage().getByClass(ElytraFlyModule.class);
        if (elytraFlyModule != null && elytraFlyModule.isEnabled()
                && elytraFlyModule.mode.get() == ElytraFlyModule.FlyMode.BOUNCE
                && (Object) this == MinecraftClient.getInstance().player
                && MC.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            cir.setReturnValue(EntityPose.STANDING);
        }
    }*/

    @Inject(method = "getLookAngle()Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void onGetRotationVector(CallbackInfoReturnable<Vec3> cir) {
        if ((Object) this != MC.player) return;
        if (ROTATION_MANAGER == null || !ROTATION_MANAGER.getStateHandler().isRotating()) return;

        float spoofYaw = ROTATION_MANAGER.getStateHandler().getRotationYaw();
        float spoofPitch = ROTATION_MANAGER.getStateHandler().getRotationPitch();

        cir.setReturnValue(((Entity) (Object) this).calculateViewVector(spoofPitch, spoofYaw));
    }

/*    @Inject(method = "lookAt", at = @At("TAIL"))
    private void onLookAt(EntityAnchorArgument.Anchor anchor, Vec3 vec3, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;

        if (!(entity instanceof LocalPlayer player)) return;
        if (player != Minecraft.getInstance().player) return;

        RotationsModule rotations = MODULE_MANAGER.getStorage().getByClass(RotationsModule.class);
        if (!rotations.render.get()) return;
        RotationStateHandler handler = ROTATION_MANAGER.getStateHandler();
        handler.setRenderPitch(player.getXRot());
        handler.setRenderHeadYaw(player.yHeadRot);
        handler.setRenderBodyYaw(player.yBodyRot);
    }*/
}
