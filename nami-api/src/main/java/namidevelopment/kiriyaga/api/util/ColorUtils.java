package namidevelopment.kiriyaga.api.util;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.awt.*;
import java.util.Optional;

public class ColorUtils {
    public static final Color COLOR_PASSIVE = new Color(211, 211, 211, 255);
    public static final Color COLOR_NEUTRAL = new Color(255, 255, 0, 255);
    public static final Color COLOR_HOSTILE = new Color(255, 0, 0, 255);
    public static final Color COLOR_ITEM = new Color(211, 211, 211, 255);
    public static final Color COLOR_FRIEND = new Color(0, 170, 170, 255);
    public static final Color COLOR_SNEAK = new Color(255, 165, 0, 255);

    public static int toRGBA(Color color) {
        return (color.getAlpha() << 24) |
                (color.getRed() << 16) |
                (color.getGreen() << 8) |
                color.getBlue();
    }

    public static Color fromRGBA(int rgba) {
        return new Color(
                (rgba >> 16) & 0xFF,
                (rgba >> 8) & 0xFF,
                rgba & 0xFF,
                (rgba >> 24) & 0xFF
        );
    }

    public static Color brighten(Color color, float amount) {
        int r = Math.min(255, (int)(color.getRed() + 255 * amount));
        int g = Math.min(255, (int)(color.getGreen() + 255 * amount));
        int b = Math.min(255, (int)(color.getBlue() + 255 * amount));
        return new Color(r, g, b, color.getAlpha());
    }

    public static Color brighten(Color color, int percent) {
        percent = Math.clamp(percent, 0, 100);

        int r = color.getRed()   + (255 - color.getRed())   * percent / 100;
        int g = color.getGreen() + (255 - color.getGreen()) * percent / 100;
        int b = color.getBlue()  + (255 - color.getBlue())  * percent / 100;

        return new Color(r, g, b, color.getAlpha());
    }

    public static TextColor brighten(TextColor color, int percent) {
        if (color == null) {
            color = TextColor.fromRgb(0xFFFFFF);
        }

        int rgb = color.getValue();
        Color c = new Color(rgb | 0xFF000000, true);
        Color bright = brighten(c, percent);

        return TextColor.fromRgb(bright.getRGB());
    }

    public static Component brighten(Component original, int brightenPercent) {
        MutableComponent result = Component.empty();

        original.visit((style, string) -> {
            TextColor baseColor = style.getColor();
            TextColor brightColor = brighten(baseColor, brightenPercent);

            Style brightStyle = style
                    .withColor(brightColor)
                    .withBold(false)
                    .withItalic(false);

            result.append(Component.literal(string).setStyle(brightStyle));
            return Optional.empty();
        }, Style.EMPTY);

        return result;
    }

    public static Color darken(Color color, float amount) {
        int r = Math.max(0, (int)(color.getRed() - 255 * amount));
        int g = Math.max(0, (int)(color.getGreen() - 255 * amount));
        int b = Math.max(0, (int)(color.getBlue() - 255 * amount));
        return new Color(r, g, b, color.getAlpha());
    }

    public static Component darken(Component original, int darkenPercent) {
        MutableComponent result = Component.empty();

        original.visit((style, string) -> {
            TextColor baseColor = style.getColor();
            TextColor shadowColor = darken(baseColor, darkenPercent);

            Style shadowStyle = style
                    .withColor(shadowColor)
                    .withBold(false)
                    .withItalic(false);

            result.append(Component.literal(string).setStyle(shadowStyle));
            return Optional.empty();
        }, Style.EMPTY);

        return result;
    }

    public static Color darken(Color color, int percent) {
        percent = Math.clamp(percent, 0, 100);
        int r = color.getRed()   * (100 - percent) / 100;
        int g = color.getGreen() * (100 - percent) / 100;
        int b = color.getBlue()  * (100 - percent) / 100;

        return new Color(r, g, b, color.getAlpha());
    }

    public static TextColor darken(TextColor color, int percent) {
        if (color == null) {
            color = TextColor.fromRgb(0xFFFFFF);
        }

        int rgb = color.getValue();
        Color c = new Color(rgb | 0xFF000000, true);
        Color dark = darken(c, percent);

        return TextColor.fromRgb(dark.getRGB());
    }

    public static String getHealthColor(Entity entity) {
        if (!(entity instanceof Player player)) return "";

        double hp = player.getHealth() + player.getAbsorptionAmount();
        double health = Math.round(hp * 2.0) / 2.0;

        if (health >= 19) return "{green}";
        if (health >= 13) return "{yellow}";
        if (health >= 8) return "{gold}";
        if (health >= 6) return "{red}";
        return "{dark_red}";
    }
}