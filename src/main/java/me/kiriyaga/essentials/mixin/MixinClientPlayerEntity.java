package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.event.impl.KeyboardInputEvent;
import me.kiriyaga.essentials.event.impl.UpdateEvent;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
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
    @Mutable
    private PlayerInput lastPlayerInput;

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickHook(CallbackInfo ci) {

        EVENT_MANAGER.post(new UpdateEvent());
    }

        @Inject(method = "tick", at = @At("HEAD"))
        private void onTick (CallbackInfo ci){
            PlayerInput oldInput = lastPlayerInput;

            KeyboardInputEvent event = new KeyboardInputEvent(
                    oldInput.forward(),
                    oldInput.backward(),
                    oldInput.left(),
                    oldInput.right(),
                    oldInput.jump(),
                    oldInput.sneak(),
                    oldInput.sprint(),
                    lastPlayerInput
            );


            EVENT_MANAGER.post(event);

            lastPlayerInput = new PlayerInput(
                    event.isForward(),
                    event.isBackward(),
                    event.isLeft(),
                    event.isRight(),
                    event.isJump(),
                    event.isSneak(),
                    event.isSprint()
            );
        }
}
