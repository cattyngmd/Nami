package me.kiriyaga.nami.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.awt.*;
import java.util.Optional;

public class ColorUtils {
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

    public static Color darken(Color color, float amount) {
        int r = Math.max(0, (int)(color.getRed() - 255 * amount));
        int g = Math.max(0, (int)(color.getGreen() - 255 * amount));
        int b = Math.max(0, (int)(color.getBlue() - 255 * amount));
        return new Color(r, g, b, color.getAlpha());
    }

    public static Text darken(Text original, int darkenPercent) {
        MutableText result = Text.empty();

        original.visit((style, string) -> {
            TextColor baseColor = style.getColor();
            TextColor shadowColor = darken(baseColor, darkenPercent);

            Style shadowStyle = style
                    .withColor(shadowColor)
                    .withBold(false)
                    .withItalic(false);

            result.append(Text.literal(string).setStyle(shadowStyle));
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

        int rgb = color.getRgb();
        Color c = new Color(rgb | 0xFF000000, true);
        Color dark = darken(c, percent);

        return TextColor.fromRgb(dark.getRGB());
    }
}