package me.kiriyaga.nami.core.rotation;

import net.minecraft.util.math.MathHelper;

import static me.kiriyaga.nami.Nami.ROTATION_MANAGER;

public class RotationStateHandler {
    private float realYaw, realPitch;
    private float rotationYaw, rotationPitch;
    private float renderYaw, renderPitch;

    public void updateRealRotation(float yaw, float pitch) {
        realYaw = wrapDegrees(yaw);
        realPitch = MathHelper.clamp(pitch, -90f, 90f);

        if (!isRotating()) {
            renderYaw = realYaw;
            renderPitch = realPitch;
        }
    }

    public float getRealYaw() {
        return realYaw;
    }

    public float getRealPitch() {
        return realPitch;
    }

    public float getRotationYaw() {
        return rotationYaw;
    }

    public float getRotationPitch() {
        return rotationPitch;
    }

    public void setRotationYaw(float yaw) {
        this.rotationYaw = wrapDegrees(yaw);
    }

    public void setRotationPitch(float pitch) {
        this.rotationPitch = MathHelper.clamp(pitch, -90f, 90f);
    }

    public float getRenderYaw() {
        return renderYaw;
    }

    public float getRenderPitch() {
        return renderPitch;
    }

    public void setRenderYaw(float yaw) {
        this.renderYaw = wrapDegrees(yaw);
    }

    public void setRenderPitch(float pitch) {
        this.renderPitch = MathHelper.clamp(pitch, -90f, 90f);
    }

    private float wrapDegrees(float angle) {
        angle %= 360f;
        if (angle >= 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }

    public boolean isRotating() {
        return ROTATION_MANAGER.getTickHandler().isRotating();
    }
}
