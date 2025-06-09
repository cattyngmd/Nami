package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.Render2DEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
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

    private static final int PADDING = 5;

    public HUDModule() {
        super("HUD","Displays in game hud.", Category.CLIENT, "ргв");
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

        if (watermark.get()) {
            String watermark = NAME + " " + VERSION;
            int x = PADDING;
            int y = PADDING;
            event.getDrawContext().drawText(MINECRAFT.textRenderer, watermark, x, y, colorInt, false);
        }

            int ping = PING_MANAGER.getPing();
            //boolean unstable = PING_MANAGER.isConnectionUnstable();

            String pingText = "Ping: " + Formatting.WHITE + ping;

            int pingX = screenWidth - MINECRAFT.textRenderer.getWidth(pingText) - PADDING;
            int pingY = screenHeight - MINECRAFT.textRenderer.fontHeight * 2 - PADDING - 2;

            event.getDrawContext().drawText(MINECRAFT.textRenderer, pingText, pingX, pingY, colorInt, false);

        if (FPS.get()) {
            int fps = MINECRAFT.getCurrentFps();
            String f = "FPS: " + Formatting.WHITE + fps;
            int x = screenWidth - MINECRAFT.textRenderer.getWidth(f) - PADDING;
            int y = screenHeight - MINECRAFT.textRenderer.fontHeight - PADDING;
            event.getDrawContext().drawText(MINECRAFT.textRenderer, f, x, y, colorInt, false);
        }

        if (cords.get()) {
            String coords = "XYZ " + Formatting.WHITE +
                    formatCoord(MINECRAFT.player.getX()) + Formatting.GRAY + ", " + Formatting.WHITE +
                    formatCoord(MINECRAFT.player.getY()) + Formatting.GRAY + ", " + Formatting.WHITE +
                    formatCoord(MINECRAFT.player.getZ());
            int x = PADDING;
            int y = screenHeight - MINECRAFT.textRenderer.fontHeight - PADDING;
            event.getDrawContext().drawText(MINECRAFT.textRenderer, coords, x, y, colorInt, false);
        }

        if (facing.get()) {
            String facing = getFacing(MINECRAFT);
            int x = PADDING;
            int y = screenHeight - MINECRAFT.textRenderer.fontHeight * 2 - PADDING - 2;
            event.getDrawContext().drawText(MINECRAFT.textRenderer, facing, x, y, colorInt, false);
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
