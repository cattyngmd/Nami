package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.kiriyaga.nami.event.impl.LedgeClipEvent;
import me.kiriyaga.nami.event.impl.LiquidPushEvent;
import me.kiriyaga.nami.event.impl.SprintResetEvent;
import me.kiriyaga.nami.impl.feature.impl.exploits.ReachFeature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.*;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity {
    protected MixinPlayer(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "isStayingOnGroundSurface", at = @At("HEAD"), cancellable = true)
    private void clipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        LedgeClipEvent ledgeClipEvent = new LedgeClipEvent();
        EVENT_SERVICE.post(ledgeClipEvent);

        if (ledgeClipEvent.isCancelled()) {
            cir.setReturnValue(ledgeClipEvent.getClipped());
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Entity target, CallbackInfo ci) {
        if ((Object)this != MC.player) return;

        SprintResetEvent sprintResetEvent = new SprintResetEvent();
        EVENT_SERVICE.post(sprintResetEvent);

        if (!sprintResetEvent.isCancelled()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            this.setSprinting(false);
        }
    }

    @Inject(method = "isPushedByFluid", at = @At("HEAD"), cancellable = true)
    private void isPushedByFluids(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this != MC.player)
            return;

        LiquidPushEvent pushFluidsEvent = new LiquidPushEvent();
        EVENT_SERVICE.post(pushFluidsEvent);
        if (pushFluidsEvent.isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @ModifyReturnValue(method = "blockInteractionRange", at = @At("RETURN"))
    private double getBlockInteractionRange(double d) {
        if (FEATURE_SERVICE == null || FEATURE_SERVICE.getStorage() == null || FEATURE_SERVICE.getStorage().getByClass(ReachFeature.class) == null || !FEATURE_SERVICE.getStorage().getByClass(ReachFeature.class).isEnabled())
            return d;

        return FEATURE_SERVICE.getStorage().getByClass(ReachFeature.class).block.get() + d;
    }

    @ModifyReturnValue(method = "entityInteractionRange", at = @At("RETURN"))
    private double getEntityInteractionRange(double d) {
        if (FEATURE_SERVICE == null || FEATURE_SERVICE.getStorage() == null || FEATURE_SERVICE.getStorage().getByClass(ReachFeature.class) == null || !FEATURE_SERVICE.getStorage().getByClass(ReachFeature.class).isEnabled())
            return d;

        return FEATURE_SERVICE.getStorage().getByClass(ReachFeature.class).entity.get() + d;
    }
}