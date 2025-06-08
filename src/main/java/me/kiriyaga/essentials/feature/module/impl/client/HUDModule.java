package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.Render2DEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
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

    public HUDModule() {
        super("HUD","Displays in game hud.", Category.CLIENT, "ргв");
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        if (MINECRAFT.world == null || MINECRAFT.player == null)
            return;

        Color c = getColorModule().getStyledPrimaryColor();

        int screenWidth = MINECRAFT.getWindow().getScaledWidth();
        int screenHeight = MINECRAFT.getWindow().getScaledHeight();

        if (watermark.get()){
            String watermark = NAME + " " + VERSION;
            int x = screenWidth / 2 - MINECRAFT.textRenderer.getWidth(watermark) / 2;
            int y = screenHeight / 2;
            event.getDrawContext().drawText(MINECRAFT.textRenderer, watermark, x, y, c.getRGB(), false);
        }

        if (cords.get()){
            String coords = Formatting.GRAY + "XYZ " + Formatting.WHITE +
                    formatCoord(MINECRAFT.player.getX()) + Formatting.GRAY + ", " + Formatting.WHITE +
                    formatCoord(MINECRAFT.player.getY()) + Formatting.GRAY + ", " + Formatting.WHITE +
                    formatCoord(MINECRAFT.player.getZ());
            event.getDrawContext().drawText(MINECRAFT.textRenderer, coords, 2, screenHeight - MINECRAFT.textRenderer.fontHeight -2 , c.getRGB(), false);
        }

        if (facing.get()){
            String facing = getFacing(MINECRAFT);
            event.getDrawContext().drawText(MINECRAFT.textRenderer, facing, 2 , screenHeight - MINECRAFT.textRenderer.fontHeight * 2 - 4, c.getRGB(), false);
        }

        if (FPS.get()){
            int fps = MINECRAFT.getCurrentFps();
            String f = "FPS: " + fps;
            event.getDrawContext().drawText(MINECRAFT.textRenderer, f, 2, screenHeight - MINECRAFT.textRenderer.fontHeight * 3 - 6, c.getRGB(), false);
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
