package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.event.impl.UpdateEvent;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.kiriyaga.essentials.Essentials.EVENT_MANAGER;
import static me.kiriyaga.essentials.Essentials.ROTATION_MANAGER;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Shadow
    public Input input;

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickHook(CallbackInfo ci) {

        EVENT_MANAGER.post(new UpdateEvent());
    }

        @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
        private void onTickMovement(CallbackInfo ci) {
        if (ROTATION_MANAGER.isRotating()){
            ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;

            Vec2f movementInput = self.input.getMovementInput();
            float sideways = movementInput.x;
            float forward = movementInput.y;


            if (forward == 0 && sideways == 0) {
                return;
            }

            float playerYaw = ROTATION_MANAGER.getRenderYaw();

            double rad = Math.toRadians(playerYaw);
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);

            double moveX = sideways * cos - forward * sin;
            double moveZ = forward * cos + sideways * sin;

            Vec3d movement = new Vec3d(moveX, 0, moveZ);

            self.move(MovementType.SELF, movement);

            ci.cancel();
        }
    }
}
