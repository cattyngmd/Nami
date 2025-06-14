package me.kiriyaga.essentials.feature.module.impl.render;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.KeyboardInputEvent;
import me.kiriyaga.essentials.event.impl.PreTickEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class FreecamModule extends Module {
    private final DoubleSetting speed = addSetting(new DoubleSetting("Speed", 1.0, 0.1, 15.0));

    private Perspective previousPerspective;
    private Vec3d cameraPos;
    public Vec3d pos = Vec3d.ZERO;
    public Vec3d prevPos = Vec3d.ZERO;
    public float yaw, pitch;
    public float lastYaw, lastPitch;

    private double camX, camY, camZ;

    private boolean forward, back, left, right, up, down;

    public FreecamModule() {
        super("Freecam", "Fly around freely without moving your player.", Category.RENDER, "freecum", "акуусгь");
    }

    @Override
    public void onEnable() {
        if (MINECRAFT.player == null) {
            return;
        }

        previousPerspective = MINECRAFT.options.getPerspective();
        MINECRAFT.options.setPerspective(Perspective.THIRD_PERSON_BACK);

        cameraPos = MINECRAFT.player.getCameraPosVec(1.0f);
        camX = cameraPos.x;
        camY = cameraPos.y;
        camZ = cameraPos.z;

        yaw = MINECRAFT.player.getYaw();
        pitch = MINECRAFT.player.getPitch();
    }

    @Override
    public void onDisable() {
        if (MINECRAFT.player == null)
            return;
        if (MINECRAFT.options.getPerspective() != previousPerspective && previousPerspective != null) {
            MINECRAFT.options.setPerspective(previousPerspective);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onUpdate(PreTickEvent event) {
        if (MINECRAFT.player == null) return;
        if (cameraPos == null) {
            toggle();
            return;
        }

        forward = MINECRAFT.options.forwardKey.isPressed();
        back    = MINECRAFT.options.backKey.isPressed();
        left    = MINECRAFT.options.leftKey.isPressed();
        right   = MINECRAFT.options.rightKey.isPressed();
        up      = MINECRAFT.options.jumpKey.isPressed();
        down    = MINECRAFT.options.sneakKey.isPressed();

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

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onKeyboard(KeyboardInputEvent event) {
        event.setForward(false);
        event.setBackward(false);
        event.setLeft(false);
        event.setRight(false);
        event.setJump(false);
        event.setSneak(false);
        event.setSprint(false);

        event.setCancelled(true);
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
