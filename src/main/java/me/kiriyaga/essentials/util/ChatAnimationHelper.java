package me.kiriyaga.essentials.util;

public class ChatAnimationHelper {
    private static volatile float animationOffset = 20f;

    public static void setAnimationOffset(float offset) {
        animationOffset = offset;
    }

    public static float getAnimationOffset() {
        return animationOffset;
    }
}
