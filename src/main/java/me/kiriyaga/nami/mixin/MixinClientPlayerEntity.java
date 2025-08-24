package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.kiriyaga.nami.event.impl.*;
import me.kiriyaga.nami.feature.module.impl.combat.ReachModule;
import me.kiriyaga.nami.feature.module.impl.movement.NoSlowModule;
import me.kiriyaga.nami.feature.module.impl.visuals.PortalGuiModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.*;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {

    @Shadow private float lastYawClient;
    @Shadow private float lastPitchClient;

    @Shadow
    protected MinecraftClient client;

    @Shadow public abstract void move(MovementType type, Vec3d movement);

    private float originalYaw, originalPitch;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {

        EVENT_MANAGER.post(new PreTickEvent());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick2(CallbackInfo ci) {

        EVENT_MANAGER.post(new PostTickEvent());
    }

    @Inject(method = "pushOutOfBlocks", at = @At(value = "HEAD"), cancellable = true)
    private void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
        BlockPushEvent pushOutOfBlocksEvent = new BlockPushEvent();
        EVENT_MANAGER.post(pushOutOfBlocksEvent);

        if (pushOutOfBlocksEvent.isCancelled())
            ci.cancel();
    }

    @Inject(method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
    private void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        MoveEvent moveEvent = new MoveEvent(movementType, movement);
        EVENT_MANAGER.post(moveEvent);

        if (moveEvent.isCancelled()) {
            ci.cancel();
            return;
        }

        Vec3d newMovement = moveEvent.getMovement();
        if (!newMovement.equals(movement)) {
            this.move(movementType, newMovement);
            ci.cancel();
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void preSendMovementPackets(CallbackInfo ci) {
        if (!ROTATION_MANAGER.getStateHandler().isRotating())
            return;

        originalYaw = MC.player.getYaw();
        originalPitch = MC.player.getPitch();

        MC.player.setYaw(ROTATION_MANAGER.getStateHandler().getRotationYaw());
        MC.player.setPitch(ROTATION_MANAGER.getStateHandler().getRotationPitch());

        MC.player.setBodyYaw(ROTATION_MANAGER.getStateHandler().getRotationYaw());
        MC.player.setHeadYaw(ROTATION_MANAGER.getStateHandler().getRotationYaw());
    }

    @Inject(method = "sendMovementPackets", at = @At("TAIL"))
    private void postSendMovementPackets(CallbackInfo ci) {
        if (!ROTATION_MANAGER.getStateHandler().isRotating())
            return;

        MC.player.setYaw(originalYaw);
        MC.player.setPitch(originalPitch);
    }

    @Inject(method = "applyMovementSpeedFactors", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec2f;multiply(F)Lnet/minecraft/util/math/Vec2f;", ordinal = 1), cancellable = true)    private void onApplyMovementSpeedFactors(Vec2f vec2f, CallbackInfoReturnable<Vec2f> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (self instanceof PlayerEntity player && player.isUsingItem() && !player.hasVehicle()) {
            ItemUseSlowEvent event = new ItemUseSlowEvent(player, player.getActiveItem());
            EVENT_MANAGER.post(event);

            if (event.isCancelled()) {
                Vec2f vec2f2 = vec2f.multiply(0.98F);
                cir.setReturnValue(applyDirectionalMovementSpeedFactors(vec2f2));
            }
        }
    }

    @Shadow
    private static Vec2f applyDirectionalMovementSpeedFactors(Vec2f vec2f) {
        throw new AssertionError();
    }

    @Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
    private void shouldSlowDown(CallbackInfoReturnable<Boolean> info) {
        if (MODULE_MANAGER == null || MODULE_MANAGER.getStorage() == null || MODULE_MANAGER.getStorage().getByClass(NoSlowModule.class) == null || !MODULE_MANAGER.getStorage().getByClass(NoSlowModule.class).isEnabled() || !MODULE_MANAGER.getStorage().getByClass(NoSlowModule.class).fastCrawl.get())
            return;

        boolean b = !MC.player.isCrawling();
        if (b) return;

        info.setReturnValue(b);
    }

    @ModifyExpressionValue(method = "tickNausea", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"))
    private Screen tickNausea(Screen s) {
        if (MODULE_MANAGER == null)
            return s;

        if (MODULE_MANAGER.getStorage().getByClass(PortalGuiModule.class).isEnabled())
            return null;

        return s;
    }
}
