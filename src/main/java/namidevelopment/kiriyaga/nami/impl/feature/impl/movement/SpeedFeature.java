package namidevelopment.kiriyaga.nami.impl.feature.impl.movement;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.feature.impl.client.RotationsFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.EnumSetting;
import namidevelopment.kiriyaga.nami.api.rotation.model.RotationRequest;
import namidevelopment.kiriyaga.nami.util.InputCache;
import net.minecraft.util.Mth;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterFeature
public class SpeedFeature extends Feature {

    private enum Mode {
        ROTATION
    }

    private final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("Mode", Mode.ROTATION));
    private final BoolSetting inLiquid = addSetting(new BoolSetting("InWater", true));

    public SpeedFeature() {
        super("Speed", "Increases movement speed.", FeatureCategory.of("Movement"));
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null) return;
        this.clearDisplayInfo();

        if (MC.player.isVisuallyCrawling() || MC.player.isCrouching() || MC.player.isShiftKeyDown() || MC.player.isFallFlying())
            return; // this fallback need due to sprinting not apply for theese states
        // also we do not need swimming because swimming do apply speed for sprinitng

        if (!inLiquid.get() && MC.player.isInWater())
            return;

        this.addDisplayInfo(mode.get().toString());

        if (mode.get() == Mode.ROTATION && isMoving()) {
            float yaw = getYaw();
            float pitch = MC.player.getXRot();
            ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(SpeedFeature.class.getName(), 1, yaw, pitch, RotationsFeature.RotationMode.MOTION));
        }
    }

    private boolean isMoving() {
        return MC.options.keyUp.isDown() ||
                MC.options.keyDown.isDown() ||
                MC.options.keyLeft.isDown() ||
                MC.options.keyRight.isDown();
    }

    private float getYaw() {
        float realYaw = MC.player.getYRot();

        boolean forward = InputCache.forward;
        boolean back = InputCache.back;
        boolean left = InputCache.left;
        boolean right = InputCache.right;

        int inputX = (right ? 1 : 0) - (left ? 1 : 0);
        int inputZ = (forward ? 1 : 0) - (back ? 1 : 0);

        if (inputX == 0 && inputZ == 0) return realYaw;

        if (inputZ > 0) return realYaw;

        if (inputZ < 0) return Mth.wrapDegrees(realYaw + 180);

        if (inputX != 0 && inputZ == 0) return Mth.wrapDegrees(realYaw + (inputX > 0 ? 90 : -90));

        if (inputZ > 0 && inputX != 0) return realYaw;

        if (inputZ < 0 && inputX != 0) return Mth.wrapDegrees(realYaw + 180);

        return realYaw;
    }
}
