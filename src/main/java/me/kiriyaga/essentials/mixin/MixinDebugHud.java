package me.kiriyaga.essentials.mixin;

import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

@Mixin(DebugHud.class)
public class MixinDebugHud implements DebugHudAccessor {

    private double x = 0;
    private double y = 0;
    private double z = 0;

    private float yaw = 0;
    private float pitch = 0;

    private boolean override = false;

    @Override
    public void setCoords(double x, double y, double z) {
        x = x;
        y = y;
        z = z;
        override = true;
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        yaw = yaw;
        pitch = pitch;
        override = true;
    }

    @Override
    public double getX() {
        return override ? x : MINECRAFT.player.getX();
    }

    @Override
    public double getY() {
        return override ? y : MINECRAFT.player.getY();
    }

    @Override
    public double getZ() {
        return override ? z : MINECRAFT.player.getZ();
    }

    @Override
    public float getYaw() {
        return override ? yaw : MINECRAFT.player.getYaw();
    }

    @Override
    public float getPitch() {
        return override ? pitch : MINECRAFT.player.getPitch();
    }

    public void disableOverride() {
        override = false;
    }
}
