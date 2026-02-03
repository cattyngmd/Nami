package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.api.event.impl.GameTimeEvent;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static namidevelopment.kiriyaga.api.NamiApi.EVENT_SERVICE;

@Mixin(DeltaTracker.Timer.class)
public abstract class MixinDeltaTracker {

    @Shadow
    private float deltaTicks;

    @Shadow
    private float deltaTickResidual;

    @Inject(method = "advanceGameTime", at = @At("TAIL"))
    private void onAdvanceGameTime(long time, CallbackInfoReturnable<Integer> cir) {
        GameTimeEvent ev = new GameTimeEvent(1.0F);
        EVENT_SERVICE.post(ev);

        if (ev.isCancelled()) {
            deltaTicks *= ev.getTicks();
            deltaTickResidual *= ev.getTicks();
        }
    }
}
