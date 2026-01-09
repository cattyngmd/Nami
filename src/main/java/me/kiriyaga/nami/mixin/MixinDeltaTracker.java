package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.GameTimeEvent;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.EVENT_MANAGER;

@Mixin(DeltaTracker.Timer.class)
public abstract class MixinDeltaTracker {

    @Shadow
    private float deltaTicks;

    @Inject(method = "advanceGameTime", at = @At("RETURN"))
    private void onAdvanceGameTime(long time, CallbackInfoReturnable<Integer> cir) {
        GameTimeEvent ev = new GameTimeEvent(-1);

        EVENT_MANAGER.post(ev);

        if (ev.isCancelled())
            deltaTicks *= ev.getTicks();
    }
}
