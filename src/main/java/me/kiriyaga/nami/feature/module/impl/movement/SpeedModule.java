package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import me.kiriyaga.nami.core.rotation.RotationRequest;
import me.kiriyaga.nami.util.InputCache;
import net.minecraft.util.math.MathHelper;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.ROTATION_MANAGER;

@RegisterModule
public class SpeedModule extends Module {

    private enum Mode {
        ROTATION
    }

    private final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("mode", Mode.ROTATION));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotate", 1, 0, 10));

    public SpeedModule() {
        super("speed", "Increases movement speed.", ModuleCategory.of("movement"));
        rotationPriority.setShowCondition(() -> mode.get() == Mode.ROTATION);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null) return;

        if (mode.get() == Mode.ROTATION && isMoving()) {
            float yaw = getYaw();
            float pitch = MC.player.getPitch();

            ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(SpeedModule.class.getName(), rotationPriority.get(), yaw, pitch));
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

        float moveAngle = (float) Math.toDegrees(Math.atan2(inputX, inputZ));
        float movementYaw = realYaw + moveAngle;

        movementYaw = MathHelper.wrapDegrees(movementYaw);
        return movementYaw;
    }
}
