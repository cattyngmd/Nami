package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.Debug;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.util.InputCache;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class SpeedModule extends Module {

    private enum Mode {
        ROTATION
    }

    private final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("mode", Mode.ROTATION));

    public SpeedModule() {
        super("speed", "Increases movement speed.", ModuleCategory.of("movement"));
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null) return;

        if (MC.player.isCrawling() || MC.player.isInSneakingPose() || MC.player.isSneaking() || MC.player.isGliding())
            return; // this fallback need due to sprinting not apply for theese states
        // also we do not need swimming because swimming do apply speed for sprinitng

        this.setDisplayInfo(mode.get().toString());

        if (mode.get() == Mode.ROTATION && isMoving()) {
            float yaw = getYaw();
            float pitch = MC.player.getPitch();
            ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(SpeedModule.class.getName(), 1, yaw, pitch));

            MODULE_MANAGER.getStorage().getByClass(Debug.class).debugSpeedRot(Text.of("Yaw diff: " + Math.abs(((MC.player.getYaw() - getYaw() + 540) % 360) - 180) ));
        }
    }

    private boolean isMoving() {
        return MC.options.forwardKey.isPressed() ||
                MC.options.backKey.isPressed() ||
                MC.options.leftKey.isPressed() ||
                MC.options.rightKey.isPressed();
    }

    private float getYaw() {
        float realYaw = MC.player.getYaw();

        boolean forward = InputCache.forward;
        boolean back = InputCache.back;
        boolean left = InputCache.left;
        boolean right = InputCache.right;

        int inputX = (right ? 1 : 0) - (left ? 1 : 0);
        int inputZ = (forward ? 1 : 0) - (back ? 1 : 0);

        if (inputX == 0 && inputZ == 0) return realYaw;

        if (inputZ > 0) return realYaw;

        if (inputZ < 0) return MathHelper.wrapDegrees(realYaw + 180);

        if (inputX != 0 && inputZ == 0) return MathHelper.wrapDegrees(realYaw + (inputX > 0 ? 90 : -90));

        if (inputZ > 0 && inputX != 0) return realYaw;

        if (inputZ < 0 && inputX != 0) return MathHelper.wrapDegrees(realYaw + 180);

        return realYaw;
    }

}
