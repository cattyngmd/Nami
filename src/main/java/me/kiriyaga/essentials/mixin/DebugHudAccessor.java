package me.kiriyaga.essentials.mixin;

public interface DebugHudAccessor {
    void setCoords(double x, double y, double z);
    double getX();
    double getY();
    double getZ();

    void setRotation(float yaw, float pitch);
    float getYaw();
    float getPitch();
}
