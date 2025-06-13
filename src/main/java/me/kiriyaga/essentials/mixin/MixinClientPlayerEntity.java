package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.event.impl.KeyboardInputEvent;
import me.kiriyaga.essentials.event.impl.PostTickEvent;
import me.kiriyaga.essentials.event.impl.PreTickEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.kiriyaga.essentials.Essentials.EVENT_MANAGER;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Shadow
    @Mutable
    private PlayerInput lastPlayerInput;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickHookPre(CallbackInfo ci) {

        EVENT_MANAGER.post(new PreTickEvent());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickHookPost(CallbackInfo ci) {

        EVENT_MANAGER.post(new PostTickEvent());
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
