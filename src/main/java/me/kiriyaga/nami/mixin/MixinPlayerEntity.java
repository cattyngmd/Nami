package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.LedgeClipEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.EVENT_MANAGER;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world)
    {
        super(entityType, world);
    }

    @Inject(method = "clipAtLedge", at = @At(value = "HEAD"), cancellable = true)
    private void clipAtLedge(CallbackInfoReturnable<Boolean> cir)
    {
        LedgeClipEvent ledgeClipEvent = new LedgeClipEvent();
        EVENT_MANAGER.post(ledgeClipEvent);
        
        if (ledgeClipEvent.isCancelled())
            cir.setReturnValue(ledgeClipEvent.getClipped());
    }
}