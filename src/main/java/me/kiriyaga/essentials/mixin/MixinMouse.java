package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.Essentials;
import me.kiriyaga.essentials.feature.module.impl.render.FreecamModule;
import me.kiriyaga.essentials.mixininterface.IMouseDeltaAccessor;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.essentials.Essentials.ROTATION_MANAGER;

@Mixin(Mouse.class)
public class MixinMouse implements IMouseDeltaAccessor {
    @Shadow private double cursorDeltaX;
    @Shadow
    private double cursorDeltaY;

    @Override
    public double getCursorDeltaX() {
        return cursorDeltaX;
    }

    @Override
    public double getCursorDeltaY() {
        return cursorDeltaY;
    }

    @Inject(
            method = "updateMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
            ),
            cancellable = true
    )
    private void cancelCameraRotation(CallbackInfo ci) {
        if (ROTATION_MANAGER.isRotating()) {
            ci.cancel();
        }
    }
}
