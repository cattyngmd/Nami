package namidevelopment.kiriyaga.api.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import namidevelopment.kiriyaga.api.contract.FeatureContractService;
import namidevelopment.kiriyaga.api.contract.feature.RotationsFeatureConfig;
import namidevelopment.kiriyaga.api.core.macro.model.Macro;
import namidevelopment.kiriyaga.api.model.setting.KeyBindSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import namidevelopment.kiriyaga.api.model.feature.Feature;

import static namidevelopment.kiriyaga.api.NamiApi.*;
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow @Nullable public LocalPlayer player;
    @Shadow public ClientLevel level;


    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void onHandleInputEvents_TAIL(CallbackInfo ci) {
        if (MC == null || MC.mouseHandler == null || MC.screen != null) return;

        for (Feature Feature : FEATURE_SERVICE.getStorage().getAll()) {
            if (Feature == null) continue;
            KeyBindSetting bind = Feature.getKeyBind();
            if (bind == null) continue;

            if (bind.get() != KeyBindSetting.KEY_NONE) {
                boolean currentlyPressed = bind.isPressed();

                if (bind.isHoldMode()) {
                    if (currentlyPressed && !Feature.isEnabled()) {
                        Feature.setEnabled(true);
                    } else if (!currentlyPressed && Feature.isEnabled()) {
                        Feature.setEnabled(false);
                    }
                } else {
                    if (currentlyPressed && !bind.wasPressedLastTick()) {
                        Feature.toggle();
                    }
                }

                bind.setWasPressedLastTick(currentlyPressed);
            }
        }

        for (Macro macro : MACRO_SERVICE.getAll()) {
            int keyCode = macro.getKeyCode();
            boolean currentlyPressed = MACRO_SERVICE.isKeyPressed(keyCode);
            boolean wasPressed = MACRO_SERVICE.wasKeyPressedLastTick(keyCode);

            if (currentlyPressed && !wasPressed) {
                if (MC.player != null) {
                    MC.player.connection.sendChat(macro.getMessage());
                }
            }

            MACRO_SERVICE.setKeyPressedLastTick(keyCode, currentlyPressed);
        }
    }

    // For futre rotations
    @Inject(method = "tick", at = @At("HEAD"))
    private void tick$exportNamiRotationsToShared(CallbackInfo ci,
                                                  @Share(namespace = "shared_rotations", value = "target_rotation")
                                                  final LocalRef<Vec2> targetRotation,

                                                  @Share(namespace = "shared_rotations", value = "target_rotation_priority")
                                                  final LocalRef<Vector2i> targetRotationPriority
    ) {
        RotationsFeatureConfig config = FeatureContractService.get(RotationsFeatureConfig.class);

        if (!ROTATION_SERVICE.getStateHandler().isRotating() || !config.isFutureRotations()) {
            return;
        }
        float yaw = ROTATION_SERVICE.getStateHandler().getRotationYaw();
        float pitch = ROTATION_SERVICE.getStateHandler().getRotationPitch();
        targetRotation.set(new Vec2(yaw, pitch));
        Integer priority = ROTATION_SERVICE.getRequestHandler().getActiveRequest().priority;
        int p = (priority == null) ? Integer.MIN_VALUE : priority;
        targetRotationPriority.set(new Vector2i(p, p));
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=gameRenderer"))
    private void tick$postInputTick(CallbackInfo ci,
                                    @Share(namespace = "shared_rotations", value = "target_rotation")
                                    final LocalRef<Vec2> _targetRotation) {
        RotationsFeatureConfig config = FeatureContractService.get(RotationsFeatureConfig.class);

        if (!config.isFutureRotations() || _targetRotation == null || !ROTATION_SERVICE.getStateHandler().isRotating())
            return;

        Vec2 rot = _targetRotation.get();

        if (rot == null) {
            return;
        }

        ROTATION_SERVICE.getStateHandler().setServerYaw(rot.x);
        ROTATION_SERVICE.getStateHandler().setServerPitch(rot.y);
    }
}