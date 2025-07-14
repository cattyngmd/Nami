package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.EntityPushEvent;
import me.kiriyaga.nami.feature.module.impl.render.FreeLookModule;
import me.kiriyaga.nami.feature.module.impl.render.FreecamModule;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void updateChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if ((Object) this != MINECRAFT.player) return;

        FreecamModule freecamModule = MODULE_MANAGER.getModule(FreecamModule.class);
        FreeLookModule freeLookModule = MODULE_MANAGER.getModule(FreeLookModule.class);

        if (freecamModule.isEnabled()) {
            freecamModule.changeLookDirection(cursorDeltaX * 0.15, cursorDeltaY * 0.15);
            ci.cancel();
        }
        else if (freeLookModule.isEnabled()) {
            freeLookModule.cameraYaw += (float) (cursorDeltaX / freeLookModule.sensivity.get().floatValue());
            freeLookModule.cameraPitch += (float) (cursorDeltaY / freeLookModule.sensivity.get().floatValue());

            if (Math.abs(freeLookModule.cameraPitch) > 90.0F) freeLookModule.cameraPitch = freeLookModule.cameraPitch > 0.0F ? 90.0F : -90.0F;
            ci.cancel();
        }
    }

    @Inject(method = "pushAwayFrom", at = @At(value = "HEAD"), cancellable = true)
    private void pushAwayFrom(Entity e, CallbackInfo ci) {
        EntityPushEvent pushEntityEvent = new EntityPushEvent((Entity)(Object) this, e);
        EVENT_MANAGER.post(pushEntityEvent);
        if (pushEntityEvent.isCancelled())
            ci.cancel();
    }
}
