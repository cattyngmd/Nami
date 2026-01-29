package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.nami.event.impl.BreakBlockEvent;
import namidevelopment.kiriyaga.nami.event.impl.PlaceBlockEvent;
import namidevelopment.kiriyaga.nami.event.impl.StartBreakingBlockEvent;
import namidevelopment.kiriyaga.nami.impl.feature.impl.world.NoBreakDelayFeature;
import namidevelopment.kiriyaga.nami.mixininterface.IClientPlayerInteractionManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static namidevelopment.kiriyaga.nami.Nami.*;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode implements IClientPlayerInteractionManager {
    @Shadow
    private int destroyDelay;

    private float savedYaw, savedPitch;

    @Shadow protected abstract void ensureHasSentCarriedItem();

    @Override
    public void updateSlot() {
        this.ensureHasSentCarriedItem();
    }

    @Inject(method = "useItem", at = @At("HEAD"))
    private void interactItem1(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player != MC.player) return;
        if (!ROTATION_SERVICE.getStateHandler().isRotating()) return;

        savedYaw = player.getYRot();
        savedPitch = player.getXRot();

        float spoofYaw = ROTATION_SERVICE.getStateHandler().getRotationYaw();
        float spoofPitch = ROTATION_SERVICE.getStateHandler().getRotationPitch();

        player.setYRot(spoofYaw);
        player.setXRot(spoofPitch);
    }

    @Inject(method = "useItem", at = @At("RETURN"))
    private void interactItem2(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player != MC.player) return;
        if (!ROTATION_SERVICE.getStateHandler().isRotating()) return;

        player.setYRot(savedYaw);
        player.setXRot(savedPitch);
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> call){
        StartBreakingBlockEvent ev = new StartBreakingBlockEvent(blockPos, direction);
        EVENT_SERVICE.post(ev);

        if (ev.isCancelled())
            call.cancel();
    }

    @Inject(method = "useItemOn", at = @At(value = "HEAD"), cancellable = true)
    private void interactBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        PlaceBlockEvent interactBlockEvent = new PlaceBlockEvent(player, hand, hitResult);
        EVENT_SERVICE.post(interactBlockEvent);

        if (interactBlockEvent.isCancelled()) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            cir.cancel();
        }
    }

    @Inject(method = "destroyBlock", at = @At(value = "HEAD"), cancellable = true)
    private void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BreakBlockEvent breakBlockEvent = new BreakBlockEvent(pos);
        EVENT_SERVICE.post(breakBlockEvent);
        if (breakBlockEvent.isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void disableBreakCooldown(CallbackInfoReturnable<Boolean> cir) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        //CHAT_SERVICE.sendRaw(""+this.blockBreakingCooldown);

        NoBreakDelayFeature noBreakDelay = FEATURE_SERVICE.getStorage().getByClass(NoBreakDelayFeature.class);
        if (noBreakDelay != null && noBreakDelay.isEnabled()) {
            this.destroyDelay = 0;
        }
    }
}
