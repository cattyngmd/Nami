package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.manager.RotationManager;
import me.kiriyaga.essentials.mixininterface.IMouseDeltaAccessor;
import me.kiriyaga.essentials.setting.impl.KeyBindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.kiriyaga.essentials.feature.module.Module;

import static me.kiriyaga.essentials.Essentials.*;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Shadow @Final public Mouse mouse;
    private double lastMouseX = -1;
    private double lastMouseY = -1;

    @Inject(method = "handleInputEvents", at = @At("TAIL"))
    private void onHandleInputEvents_TAIL(CallbackInfo ci) {
        if (MINECRAFT == null || MINECRAFT.mouse == null) return;

        for (Module module : MODULE_MANAGER.getModules()) {
            KeyBindSetting bind = module.getKeyBind();
            if (bind.get() != KeyBindSetting.KEY_NONE) {
                boolean currentlyPressed = bind.isPressed();

                if (bind.isHoldMode()) {
                    if (currentlyPressed && !module.isEnabled()) {
                        module.setEnabled(true);
                    } else if (!currentlyPressed && module.isEnabled()) {
                        module.setEnabled(false);
                    }
                } else {
                    if (currentlyPressed && !bind.wasPressedLastTick()) {
                        module.toggle();
                    }
                }
                bind.setWasPressedLastTick(currentlyPressed);
            }
        }
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onHandleInputEvents_HEAD(CallbackInfo ci) {
        if (MINECRAFT == null || MINECRAFT.mouse == null) return;

        Mouse mouse = this.mouse;
        if (mouse == null) return;

        if (!(mouse instanceof IMouseDeltaAccessor)) return;

        double deltaX = ((IMouseDeltaAccessor) mouse).getCursorDeltaX();
        double deltaY = ((IMouseDeltaAccessor) mouse).getCursorDeltaY();


        if (ROTATION_MANAGER != null && ROTATION_MANAGER.isRotating()) {
            float mouseSensitivity = MINECRAFT.options.getMouseSensitivity().getValue().floatValue();
            float adjustedSensitivity = (mouseSensitivity * 0.6F + 0.2F) * 0.6F;
            float cubicSensitivity = adjustedSensitivity * adjustedSensitivity * adjustedSensitivity;
            float degreesPerPixel = cubicSensitivity * 8.0F;

            float yawDelta = (float) deltaX * degreesPerPixel;
            float pitchDelta = (float) deltaY * degreesPerPixel;

            float newYaw = ROTATION_MANAGER.getRenderYaw() + yawDelta;
            float newPitch = ROTATION_MANAGER.getRenderPitch() + pitchDelta;

            newPitch = MathHelper.clamp(newPitch, -90.0F, 90.0F);
            newYaw = MathHelper.wrapDegrees(newYaw);

            ROTATION_MANAGER.setRenderYaw(newYaw);
            ROTATION_MANAGER.setRenderPitch(newPitch);
        }
    }


}
