package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.Render2DEvent;
import me.kiriyaga.essentials.event.impl.UpdateEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

import static me.kiriyaga.essentials.Essentials.*;

public class HUDModule extends Module {

    private ColorModule getColorModule() {
        return MODULE_MANAGER.getModule(ColorModule.class);
    }

    public final BoolSetting watermark = addSetting(new BoolSetting("Watermark", true));
    public final BoolSetting cords = addSetting(new BoolSetting("Coordinates", false));
    public final BoolSetting facing = addSetting(new BoolSetting("Facing", true));
    public final BoolSetting FPS = addSetting(new BoolSetting("FPS", true));
    public final IntSetting updateInterval = addSetting(new IntSetting("Update Interval", 5, 1, 20));

    private static final int PADDING = 5;

    private int tickCounter = 0;

    // since updating each tick (or even each render tick) means updating 20/60 times per second, we better timeout theese interactions
    private String watermarkText = "";
    private String coordsText = "";
    private String facingText = "";
    private String fpsText = "";
    private String pingText = "";

    public HUDModule() {
        super("HUD", "Displays in game hud.", Category.CLIENT, "ргв");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdate(UpdateEvent event) {
        tickCounter++;
        if (tickCounter % updateInterval.get() != 0) return;

        if (MINECRAFT.world == null || MINECRAFT.player == null)
            return;

        if (watermark.get()) {
            watermarkText = NAME + " " + VERSION;
        } else {
            watermarkText = null;
        }

        if (cords.get()) {
            coordsText = "XYZ " + Formatting.WHITE +
                    formatCoord(MINECRAFT.player.getX()) + Formatting.GRAY + ", " + Formatting.WHITE +
                    formatCoord(MINECRAFT.player.getY()) + Formatting.GRAY + ", " + Formatting.WHITE +
                    formatCoord(MINECRAFT.player.getZ());
        } else {
            coordsText = null;
        }

        if (facing.get()) {
            facingText = getFacing(MINECRAFT);
        } else {
            facingText = null;
        }

        if (FPS.get()) {
            fpsText = "FPS: " + Formatting.WHITE + MINECRAFT.getCurrentFps();
        } else {
            fpsText = null;
        }

        int ping = PING_MANAGER.getPing();
        pingText = "Ping: " + Formatting.WHITE + ping;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender2D(Render2DEvent event) {
        if (MINECRAFT.world == null || MINECRAFT.player == null)
            return;

        Color styled = getColorModule().getStyledPrimaryColor();
        Color opaque = new Color(styled.getRed(), styled.getGreen(), styled.getBlue(), 255);
        int colorInt = opaque.getRGB();

        int screenWidth = MINECRAFT.getWindow().getScaledWidth();
        int screenHeight = MINECRAFT.getWindow().getScaledHeight();

        int yOffset = PADDING;

        if (watermarkText != null) {
            event.getDrawContext().drawText(MINECRAFT.textRenderer, watermarkText, PADDING, yOffset, colorInt, false);
            yOffset += MINECRAFT.textRenderer.fontHeight + 2;
        }

        if (facingText != null) {
            event.getDrawContext().drawText(MINECRAFT.textRenderer, facingText, PADDING, screenHeight - MINECRAFT.textRenderer.fontHeight * 2 - PADDING - 2, colorInt, false);
        }

        if (coordsText != null) {
            event.getDrawContext().drawText(MINECRAFT.textRenderer, coordsText, PADDING, screenHeight - MINECRAFT.textRenderer.fontHeight - PADDING, colorInt, false);
        }

        if (pingText != null) {
            int pingX = screenWidth - MINECRAFT.textRenderer.getWidth(pingText) - PADDING;
            int pingY = screenHeight - MINECRAFT.textRenderer.fontHeight * 2 - PADDING - 2;
            event.getDrawContext().drawText(MINECRAFT.textRenderer, pingText, pingX, pingY, colorInt, false);
        }

        if (fpsText != null) {
            int fpsX = screenWidth - MINECRAFT.textRenderer.getWidth(fpsText) - PADDING;
            int fpsY = screenHeight - MINECRAFT.textRenderer.fontHeight - PADDING;
            event.getDrawContext().drawText(MINECRAFT.textRenderer, fpsText, fpsX, fpsY, colorInt, false);
        }
    }

    private String formatCoord(double coord) {
        return String.format("%.1f", coord).replace(',', '.');
    }

    private String getFacing(MinecraftClient mc) {
        if (mc.player == null) return "Unknown";
        int facingIndex = MathHelper.floor(mc.player.getYaw() * 4.0F / 360.0F + 0.5D) & 3;
        return switch (facingIndex) {
            case 0 -> "South";
            case 1 -> "West";
            case 2 -> "North";
            case 3 -> "East";
            default -> "Invalid";
        };
    }
}
