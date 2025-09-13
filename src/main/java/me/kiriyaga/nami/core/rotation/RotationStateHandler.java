package me.kiriyaga.nami.core.rotation;

import net.minecraft.util.math.MathHelper;

import static me.kiriyaga.nami.Nami.ROTATION_MANAGER;

public class RotationStateHandler {
    private float realYaw, realPitch;
    private float rotationYaw, rotationPitch;
    private float renderYaw, renderPitch;
    private float serverYaw, serverPitch;
    private float serverDeltaYaw;

    public void updateRealRotation(float yaw, float pitch) {
        realYaw = yaw;
        realPitch = MathHelper.clamp(pitch, -90f, 90f);

        if (!isRotating()) {
            renderYaw = realYaw;
            renderPitch = realPitch;
        }
    }

    public float getRealYaw() { return realYaw; }
    public float getRealPitch() { return realPitch; }

    public float getRotationYaw() { return rotationYaw; }
    public float getRotationPitch() { return rotationPitch; }

    public void setRotationYaw(float yaw) {this.rotationYaw = yaw;}

    public void setRotationPitch(float pitch) {this.rotationPitch = MathHelper.clamp(pitch, -90f, 90f);}

    public float getRenderYaw() { return renderYaw; }
    public float getRenderPitch() { return renderPitch; }

    // WE DO NOT WRAP/NORMALIZE SERVER ROTATIONS!!!
    public void setRenderYaw(float yaw) { this.renderYaw = wrapDegrees(yaw); }
    public void setRenderPitch(float pitch) { this.renderPitch = MathHelper.clamp(pitch, -90f, 90f); }

    public float getServerYaw() {return serverYaw;}
    public void setServerYaw(float yaw) {this.serverYaw = yaw;}

    public float getServerPitch() {return serverPitch;}
    public void setServerPitch(float pitch) {this.serverPitch = pitch;}

    public float getServerDeltaYaw() {return serverDeltaYaw;}
    public void setServerDeltaYaw(float deltaYaw) {this.serverDeltaYaw = deltaYaw;}

    public static float wrapDegrees(float angle) {
        angle %= 360f;
        if (angle >= 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }

    public boolean isRotating() {
        return ROTATION_MANAGER.getTickHandler().isRotating();
    }
}
