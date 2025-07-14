package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.LedgeClipEvent;
import me.kiriyaga.nami.event.impl.LiquidPushEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.EVENT_MANAGER;
import static me.kiriyaga.nami.Nami.MINECRAFT;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "clipAtLedge", at = @At(value = "HEAD"), cancellable = true)
    private void clipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        LedgeClipEvent ledgeClipEvent = new LedgeClipEvent();
        EVENT_MANAGER.post(ledgeClipEvent);
        
        if (ledgeClipEvent.isCancelled())
            cir.setReturnValue(ledgeClipEvent.getClipped());
    }

    @Inject(method = "isPushedByFluids", at = @At(value = "HEAD"),
            cancellable = true)
    private void isPushedByFluids(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this != MINECRAFT.player)
            return;

        LiquidPushEvent pushFluidsEvent = new LiquidPushEvent();
        EVENT_MANAGER.post(pushFluidsEvent);
        if (pushFluidsEvent.isCancelled())
        {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}