package me.kiriyaga.essentials.mixin;

import net.minecraft.client.input.Input;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.essentials.Essentials.ROTATION_MANAGER;

@Mixin(Input.class) // Или KeyboardInput.class, если надо
public class MixinInput {
        @Inject(method = "getMovementInput", at = @At("HEAD"), cancellable = true)
        private void onGetMovementInput(CallbackInfoReturnable<Vec2f> cir) {
            if (ROTATION_MANAGER.isRotating()) {
                InputAccessor accessor = (InputAccessor)(Object)this;
                Vec2f originalVec = accessor.getMovementVector();
                float sideways = originalVec.x;
                float forward = originalVec.y;

                float yaw = ROTATION_MANAGER.getRenderYaw();
                double rad = Math.toRadians(yaw);
                float sin = (float)Math.sin(rad);
                float cos = (float)Math.cos(rad);

                float newX = sideways * cos - forward * sin;
                float newY = forward * cos + sideways * sin;

                cir.setReturnValue(new Vec2f(newX, newY));
            }
        }
}

