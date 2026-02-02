package namidevelopment.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import namidevelopment.kiriyaga.api.client.RotationsFeature;
import namidevelopment.kiriyaga.nami.impl.feature.movement.NoSlowFeature;
import namidevelopment.kiriyaga.nami.impl.feature.visuals.NoRenderFeature;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;
@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer {

    @Shadow public abstract void move(MoverType type, Vec3 movement);

    private float originalSilentPitch;
    private float originalYaw, originalPitch;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickHookPre(CallbackInfo ci) {

        EVENT_SERVICE.post(new PreTickEvent());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickHookPost(CallbackInfo ci) {

        EVENT_SERVICE.post(new PostTickEvent());
    }

    @Inject(method = "moveTowardsClosestSpace", at = @At(value = "HEAD"), cancellable = true)
    private void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
        BlockPushEvent pushOutOfBlocksEvent = new BlockPushEvent();
        EVENT_SERVICE.post(pushOutOfBlocksEvent);

        if (pushOutOfBlocksEvent.isCancelled())
            ci.cancel();
    }

    @Inject(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true)
    private void onMove(MoverType movementType, Vec3 movement, CallbackInfo ci) {
        MoveEvent moveEvent = new MoveEvent(movementType, movement);
        EVENT_SERVICE.post(moveEvent);

        if (moveEvent.isCancelled()) {
            ci.cancel();
            return;
        }

        Vec3 newMovement = moveEvent.getMovement();
        if (!newMovement.equals(movement)) {
            this.move(movementType, newMovement);
            ci.cancel();
        }
    }

    @Inject(method = "sendPosition", at = @At("HEAD"))
    private void preSendMovementPackets(CallbackInfo ci) {
        if (!ROTATION_SERVICE.getStateHandler().isRotating()) {
            ROTATION_SERVICE.getStateHandler().setServerDeltaYaw(0f); // delta without rotations almost always lower then 30, its almost impossible without hacks to reach
            return;
        }

        originalYaw = MC.player.getYRot();
        originalPitch = MC.player.getXRot();

        float newYaw = ROTATION_SERVICE.getStateHandler().getRotationYaw();
        float newPitch = ROTATION_SERVICE.getStateHandler().getRotationPitch();
        MC.player.setYRot(newYaw);
        MC.player.setXRot(newPitch);

        float deltaYaw = newYaw - ROTATION_SERVICE.getStateHandler().getServerYaw();
        //float deltaPitch = newPitch - ROTATION_SERVICE.getStateHandler().getServerPitch();

        ROTATION_SERVICE.getStateHandler().setServerDeltaYaw(deltaYaw);

        ROTATION_SERVICE.getStateHandler().setServerYaw(newYaw);
        ROTATION_SERVICE.getStateHandler().setServerPitch(newPitch);
    }

    @Inject(method = "sendPosition", at = @At("TAIL"))
    private void postSendMovementPackets(CallbackInfo ci) {
        ROTATION_SERVICE.getStateHandler().setServerYaw(MC.player.getYRot());
        ROTATION_SERVICE.getStateHandler().setServerPitch(MC.player.getXRot());

        if (!ROTATION_SERVICE.getStateHandler().isRotating())
            return;

        MC.player.setYRot(originalYaw);
        MC.player.setXRot(originalPitch);
    }

    // Do not ask me exactly why is it so weird, it just works
    // overall silent rotations sucks, another super cool bypass, works really weirdly
    // i hope it gets fucking patched in 1.22/1.23
    @Inject(method = "sendPosition", at = @At("HEAD"))
    private void sendMovementPackets1(CallbackInfo ci) {
        if (FEATURE_SERVICE.getStorage().getByClass(RotationsFeature.class).rotation.get() == RotationsFeature.RotationMode.SILENT
        && ROTATION_SERVICE.getStateHandler().getSilentSyncRequired()) {
            this.originalSilentPitch = MC.player.getXRot();
            this.xRotLast = -9999;
            MC.player.setXRot(this.originalSilentPitch + 1f);
        }
    }

    @Inject(method = "sendPosition", at = @At("RETURN"))
    private void sendMovementPackets2(CallbackInfo ci) {
        if (FEATURE_SERVICE.getStorage().getByClass(RotationsFeature.class).rotation.get() == RotationsFeature.RotationMode.SILENT
                && ROTATION_SERVICE.getStateHandler().getSilentSyncRequired()) {
            MC.player.setXRot(this.originalSilentPitch);
            ROTATION_SERVICE.getStateHandler().setSilentSyncRequired(false);
        }
    }

    @Inject(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec2;scale(F)Lnet/minecraft/world/phys/Vec2;", ordinal = 1), cancellable = true)    private void onApplyMovementSpeedFactors(Vec2 vec2f, CallbackInfoReturnable<Vec2> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (self instanceof Player player && player.isUsingItem() && !player.isPassenger()) {
            ItemUseSlowEvent event = new ItemUseSlowEvent(player, player.getUseItem());
            EVENT_SERVICE.post(event);

            if (event.isCancelled()) {
                Vec2 vec2f2 = vec2f.scale(0.98F);
                cir.setReturnValue(modifyInputSpeedForSquareMovement(vec2f2));
            }
        }
    }

    @Shadow
    private static Vec2 modifyInputSpeedForSquareMovement(Vec2 vec2f) {
        throw new AssertionError();
    }

    @Shadow private float xRotLast;

    @Inject(method = "isMovingSlowly", at = @At("HEAD"), cancellable = true)
    private void shouldSlowDown(CallbackInfoReturnable<Boolean> info) {
        if (FEATURE_SERVICE == null || FEATURE_SERVICE.getStorage() == null || FEATURE_SERVICE.getStorage().getByClass(NoSlowFeature.class) == null || !FEATURE_SERVICE.getStorage().getByClass(NoSlowFeature.class).isEnabled() || !FEATURE_SERVICE.getStorage().getByClass(NoSlowFeature.class).fastCrawl.get())
            return;

        boolean b = !MC.player.isVisuallyCrawling();
        if (b) return;

        info.setReturnValue(b);
    }

    @ModifyExpressionValue(method = "handlePortalTransitionEffect", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;"))
    private Screen tickNausea(Screen s) {
        if (FEATURE_SERVICE == null)
            return s;

        if (FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class).isEnabled() && FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class).portalGui.get())
            return null;

        return s;
    }
}
