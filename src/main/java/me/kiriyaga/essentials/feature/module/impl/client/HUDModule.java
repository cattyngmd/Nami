package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.Render2DEvent;
import me.kiriyaga.essentials.event.impl.UpdateEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;
import me.kiriyaga.essentials.util.ChatAnimationHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

import static me.kiriyaga.essentials.Essentials.*;

public class HUDModule extends Module {

    public final IntSetting updateInterval = addSetting(new IntSetting("Update Interval", 5, 1, 20));

    public final BoolSetting watermarkEnabled = addSetting(new BoolSetting("Watermark", true));
    public final BoolSetting coordsEnabled = addSetting(new BoolSetting("Coordinates", false));
    public final BoolSetting facingEnabled = addSetting(new BoolSetting("Facing", true));
    public final BoolSetting fpsEnabled = addSetting(new BoolSetting("FPS", true));
    public final BoolSetting pingEnabled = addSetting(new BoolSetting("Ping", true));
    public final BoolSetting lagWarningEnabled = addSetting(new BoolSetting("Lag Warning", true));

    private static final int PADDING = 5;

    private int tickCounter = 0;

    private String watermarkText = "";
    private String coordsText = "";
    private String facingText = "";
    private String fpsText = "";
    private String pingText = "";
    private boolean serverLagging = false;


    public HUDModule() {
        super("HUD","Displays in game hud.", Category.CLIENT, "ргв");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdate(UpdateEvent event) {
        tickCounter++;
        if (tickCounter % updateInterval.get() != 0) return;

        updateAllData();
    }

    private void updateAllData() {
        MinecraftClient mc = MINECRAFT;
        if (mc.world == null || mc.player == null) return;

        if (watermarkEnabled.get()) {
            watermarkText = NAME + " " + VERSION;
        }

        if (coordsEnabled.get()) {
            coordsText = String.format("XYZ %s, %s, %s",
                    formatCoord(mc.player.getX()),
                    formatCoord(mc.player.getY()),
                    formatCoord(mc.player.getZ()));
        }

        if (facingEnabled.get()) {
            facingText = getFacing(mc);
        }

        if (fpsEnabled.get()) {
            fpsText = "FPS: " + Formatting.WHITE + mc.getCurrentFps();
        }

        if (pingEnabled.get()) {
            int ping = PING_MANAGER.getPing();
            pingText = "Ping: " + Formatting.WHITE + (ping < 0 ? "N/A" : ping);
        }

        if (lagWarningEnabled.get()){
            serverLagging = PING_MANAGER.isConnectionUnstable();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender2D(Render2DEvent event) {
        MinecraftClient mc = MINECRAFT;
        if (mc.world == null || mc.player == null) return;

        Color styled = getColorModule().getStyledPrimaryColor();
        Color opaque = new Color(styled.getRed(), styled.getGreen(), styled.getBlue(), 255);
        int colorInt = opaque.getRGB();

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        float rawOffset = ChatAnimationHelper.getAnimationOffset();

        float animationOffset = 20f - rawOffset;

        renderTopLeft(event, colorInt);

        renderBottomLeft(event, colorInt, screenHeight, animationOffset);

        renderBottomRight(event, colorInt, screenWidth, screenHeight, animationOffset);

        if (lagWarningEnabled.get() && serverLagging) {
            drawLagWarning(event, screenWidth, screenHeight);
        }
    }

    private void renderTopLeft(Render2DEvent event, int color) {
        if (watermarkEnabled.get() && !watermarkText.isEmpty()) {
            event.getDrawContext().drawText(MINECRAFT.textRenderer, watermarkText, PADDING, PADDING, color, false);
        }
    }

    private void renderBottomLeft(Render2DEvent event, int color, int screenHeight, float animationOffset) {
        int y = screenHeight - MINECRAFT.textRenderer.fontHeight - PADDING;
        y -= (int) animationOffset;

        if (coordsEnabled.get() && !coordsText.isEmpty()) {
            event.getDrawContext().drawText(MINECRAFT.textRenderer, coordsText, PADDING, y, color, false);
            y -= MINECRAFT.textRenderer.fontHeight + 2;
        }

        if (facingEnabled.get() && !facingText.isEmpty()) {
            event.getDrawContext().drawText(MINECRAFT.textRenderer, facingText, PADDING, y, color, false);
            y -= MINECRAFT.textRenderer.fontHeight + 2;
        }
    }

    private void renderBottomRight(Render2DEvent event, int color, int screenWidth, int screenHeight, float animationOffset) {
        int y = screenHeight - MINECRAFT.textRenderer.fontHeight - PADDING;
        y -= (int) animationOffset;

        if (fpsEnabled.get() && !fpsText.isEmpty()) {
            int width = MINECRAFT.textRenderer.getWidth(fpsText);
            event.getDrawContext().drawText(MINECRAFT.textRenderer, fpsText, screenWidth - width - PADDING, y, color, false);
            y -= MINECRAFT.textRenderer.fontHeight + 2;
        }

        if (pingEnabled.get() && !pingText.isEmpty()) {
            int width = MINECRAFT.textRenderer.getWidth(pingText);
            event.getDrawContext().drawText(MINECRAFT.textRenderer, pingText, screenWidth - width - PADDING, y, color, false);
            y -= MINECRAFT.textRenderer.fontHeight + 2;
        }
    }

    private void drawLagWarning(Render2DEvent event, int screenWidth, int screenHeight) {
        String warningText = "Server is lagging!";
        int textWidth = MINECRAFT.textRenderer.getWidth(warningText);
        int textHeight = MINECRAFT.textRenderer.fontHeight;

        int x = (screenWidth - textWidth) / 2;
        int y = (screenHeight - textHeight) / 2;

        Color styled = getColorModule().getStyledPrimaryColor();
        Color opaque = new Color(styled.getRed(), styled.getGreen(), styled.getBlue(), 255);
        int colorInt = opaque.getRGB();

        event.getDrawContext().drawText(MINECRAFT.textRenderer, warningText, x, y, colorInt, false);
    }


    private String formatCoord(double coord) {
        return String.format("%.1f", coord).replace(',', '.');
    }

    private String getFacing(MinecraftClient mc) {
        if (mc.player == null) return "Invalid";
        int facingIndex = MathHelper.floor(mc.player.getYaw() * 4.0F / 360.0F + 0.5D) & 3;
        return switch (facingIndex) {
            case 0 -> "South";
            case 1 -> "West";
            case 2 -> "North";
            case 3 -> "East";
            default -> "Invalid";
        };
    }

    private ColorModule getColorModule() {
        return MODULE_MANAGER.getModule(ColorModule.class);
    }
}
