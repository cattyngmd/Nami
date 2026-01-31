package namidevelopment.kiriyaga.nami.impl.feature.impl.visuals;

import namidevelopment.kiriyaga.nami.api.rotation.model.RotationRequest;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.KeyInputEvent;
import namidevelopment.kiriyaga.nami.event.impl.Render3DEvent;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.feature.impl.client.RotationsFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import net.minecraft.client.CameraType;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import static namidevelopment.kiriyaga.nami.Nami.MC;
import static namidevelopment.kiriyaga.nami.Nami.ROTATION_SERVICE;

@RegisterFeature
public class FreecamFeature extends Feature {
    public final DoubleSetting speed = addSetting(new DoubleSetting("Speed", 0.5, 0.1, 5.0));
    public final DoubleSetting accelerate = addSetting(new DoubleSetting("Accelerate", 2.3, 1.0, 3.0));
    public final BoolSetting look = addSetting(new BoolSetting("Look", true));

    @SuppressWarnings("FieldCanBeLocal")
    private double currentFactor = 1.0;
    private long accelStartTime = -1;
    @SuppressWarnings("FieldCanBeLocal")
    private final double accelDuration = 0.8;
    private CameraType previousPerspective;
    private Vec3 cameraPos;
    public Vec3 pos = Vec3.ZERO;
    public Vec3 prevPos = Vec3.ZERO;
    public float yaw, pitch;
    public float lastYaw, lastPitch;

    private double camX, camY, camZ;

    private boolean forward, back, left, right, up, down;

    public FreecamFeature() {
        super("Freecam", "Fly around freely without moving your player.", FeatureCategory.of("Render"), "freecum");
    }

    @Override
    public void onEnable() {
        if (MC.player == null || MC.level == null) {
            toggle();
            return;
        }

        previousPerspective = MC.options.getCameraType();
        MC.options.setCameraType(CameraType.THIRD_PERSON_BACK);

        cameraPos = MC.player.getEyePosition(1.0f);
        camX = cameraPos.x;
        camY = cameraPos.y;
        camZ = cameraPos.z;

        yaw = MC.player.getYRot();
        pitch = MC.player.getXRot();
    }

    @Override
    public void onDisable() {
        if (MC.player == null)
            return;
        if (MC.options.getCameraType() != previousPerspective && previousPerspective != null) {
            MC.options.setCameraType(previousPerspective);
        }
    }

    @SubscribeEvent
    public void onPreTick(Render3DEvent event) {
        if (cameraPos == null || MC.player == null || MC.level == null) {
            this.toggle();
            return;
        }

        if (MC.screen instanceof ChatScreen)
            return;

        boolean moving = forward || back || left || right || up || down;

        if (moving) {
            if (accelStartTime < 0) accelStartTime = System.currentTimeMillis();
            double elapsed = (System.currentTimeMillis() - accelStartTime) / 1000.0;
            double t = Mth.clamp(elapsed / accelDuration, 0, 1);
            currentFactor = 1.0 + t * (accelerate.get() - 1.0);
        } else {
            currentFactor = 1.0;
            accelStartTime = -1;
        }

        double spd = (speed.get() * currentFactor ) /6;

        double dx = 0, dy = 0, dz = 0;
        Vec3 forwardVec = Vec3.directionFromRotation(0, yaw);
        Vec3 rightVec = Vec3.directionFromRotation(0, yaw + 90);

        if (forward) { dx += forwardVec.x * spd; dz += forwardVec.z * spd; }
        if (back)    { dx -= forwardVec.x * spd; dz -= forwardVec.z * spd; }
        if (left)    { dx -= rightVec.x * spd; dz -= rightVec.z * spd; }
        if (right)   { dx += rightVec.x * spd; dz += rightVec.z * spd; }
        if (up) dy += spd;
        if (down) dy -= spd;

        if ((forward || back) && (left || right)) {
            dx *= 0.7071;
            dz *= 0.7071;
        }

        prevPos = pos;
        pos = cameraPos;
        cameraPos = cameraPos.add(dx, dy, dz);

        camX = cameraPos.x;
        camY = cameraPos.y;
        camZ = cameraPos.z;

        if (look.get()) {
            var hit = MC.hitResult;
            if (hit != null && hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                Vec3 target = hit.getLocation();
                Vec3 from = MC.player.position().add(0, MC.player.getEyeHeight(), 0);

                double diffX = target.x - from.x;
                double diffY = target.y - from.y;
                double diffZ = target.z - from.z;

                double yawToTarget = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90;
                double pitchToTarget = -Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)));

                ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(FreecamFeature.class.getName() ,3, (float)yawToTarget, (float)pitchToTarget, RotationsFeature.RotationMode.MOTION));
            }
        }
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {

        int forwardKey = InputConstants.getKey(MC.options.keyUp.saveString()).getValue();
        int backKey    = InputConstants.getKey(MC.options.keyDown.saveString()).getValue();
        int leftKey    = InputConstants.getKey(MC.options.keyLeft.saveString()).getValue();
        int rightKey   = InputConstants.getKey(MC.options.keyRight.saveString()).getValue();
        int jumpKey    = InputConstants.getKey(MC.options.keyJump.saveString()).getValue();
        int sneakKey   = InputConstants.getKey(MC.options.keyShift.saveString()).getValue();


        boolean pressed = event.action != GLFW.GLFW_RELEASE;

        if (event.key == forwardKey) forward = pressed;
        else if (event.key == backKey) back = pressed;
        else if (event.key == leftKey) left = pressed;
        else if (event.key == rightKey) right = pressed;
        else if (event.key == jumpKey) up = pressed;
        else if (event.key == sneakKey) down = pressed;

        if (forward || back || left || right || up || down) {
            event.cancel();
        }
    }



    public double getX() { return camX; }
    public double getY() { return camY; }
    public double getZ() { return camZ; }

    public Vec3 getCameraPos() {
        return cameraPos;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        lastYaw = yaw;
        lastPitch = pitch;

        yaw += (float) deltaX;
        pitch += (float) deltaY;

        pitch = Mth.clamp(pitch, -90, 90);
    }
}
