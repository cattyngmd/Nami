package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.KeyInputEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class FreecamModule extends Module {
    private final DoubleSetting speed = addSetting(new DoubleSetting("speed", 1.0, 0.1, 5.0));

    private Perspective previousPerspective;
    private Vec3d cameraPos;
    public Vec3d pos = Vec3d.ZERO;
    public Vec3d prevPos = Vec3d.ZERO;
    public float yaw, pitch;
    public float lastYaw, lastPitch;

    private double camX, camY, camZ;

    private boolean forward, back, left, right, up, down;

    public FreecamModule() {
        super("freecam", "Fly around freely without moving your player.", ModuleCategory.of("visuals"), "freecum", "акуусгь");
    }

    @Override
    public void onEnable() {
        if (MC.player == null) {
            return;
        }

        previousPerspective = MC.options.getPerspective();
        MC.options.setPerspective(Perspective.THIRD_PERSON_BACK);

        cameraPos = MC.player.getCameraPosVec(1.0f);
        camX = cameraPos.x;
        camY = cameraPos.y;
        camZ = cameraPos.z;

        yaw = MC.player.getYaw();
        pitch = MC.player.getPitch();
    }

    @Override
    public void onDisable() {
        if (MC.player == null)
            return;
        if (MC.options.getPerspective() != previousPerspective && previousPerspective != null) {
            MC.options.setPerspective(previousPerspective);
        }
    }

    @SubscribeEvent
    public void onPreTick(PreTickEvent event) {
        if (cameraPos == null){
            this.toggle();
            return;
        }

        double dx = 0, dy = 0, dz = 0;
        Vec3d forwardVec = Vec3d.fromPolar(0, yaw);
        Vec3d rightVec = Vec3d.fromPolar(0, yaw + 90);

        double spd = speed.get();

        if (forward) {
            dx += forwardVec.x * spd;
            dz += forwardVec.z * spd;
        }
        if (back) {
            dx -= forwardVec.x * spd;
            dz -= forwardVec.z * spd;
        }
        if (left) {
            dx -= rightVec.x * spd;
            dz -= rightVec.z * spd;
        }
        if (right) {
            dx += rightVec.x * spd;
            dz += rightVec.z * spd;
        }
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
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {

        int forwardKey = InputUtil.fromTranslationKey(MC.options.forwardKey.getBoundKeyTranslationKey()).getCode();
        int backKey    = InputUtil.fromTranslationKey(MC.options.backKey.getBoundKeyTranslationKey()).getCode();
        int leftKey    = InputUtil.fromTranslationKey(MC.options.leftKey.getBoundKeyTranslationKey()).getCode();
        int rightKey   = InputUtil.fromTranslationKey(MC.options.rightKey.getBoundKeyTranslationKey()).getCode();
        int jumpKey    = InputUtil.fromTranslationKey(MC.options.jumpKey.getBoundKeyTranslationKey()).getCode();
        int sneakKey   = InputUtil.fromTranslationKey(MC.options.sneakKey.getBoundKeyTranslationKey()).getCode();


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

    public Vec3d getCameraPos() {
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

        pitch = MathHelper.clamp(pitch, -90, 90);
    }
}
