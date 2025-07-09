package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.ColorSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;

import java.awt.*;

public class ColorModule extends Module {

    public final ColorSetting globalColor = addSetting(new ColorSetting("global", new Color(23, 0, 255, 170), true));

    public final DoubleSetting globalSaturation = addSetting(new DoubleSetting("saturation", 0.7, 0.0, 1.0));
    public final DoubleSetting globalDarskness = addSetting(new DoubleSetting("darkness", 0.4, 0.0, 1.0));

    public final DoubleSetting alpha = addSetting(new DoubleSetting("alpha", 0.5, 0.0, 1.0));

    public ColorModule() {
        super("color", "Customizes color scheme.", Category.client, "colr", "c", "colors", "clitor", "сщдщк");
    }

    private int getAlpha255() {
        return (int) (Math.max(0.0, Math.min(1.0, alpha.get())) * 255);
    }

    public Color applySaturation(Color base, double saturationFactor) {
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        float saturation = (float) (hsb[1] * saturationFactor);
        saturation = Math.max(0f, Math.min(1f, saturation));
        return Color.getHSBColor(hsb[0], saturation, hsb[2]);
    }

    public Color applyDarkness(Color base, double darknessFactor) {
        float factor = (float) (1.0 - darknessFactor);
        int r = Math.max(0, (int) (base.getRed() * factor));
        int g = Math.max(0, (int) (base.getGreen() * factor));
        int b = Math.max(0, (int) (base.getBlue() * factor));
        return new Color(r, g, b);
    }

    public Color getStyledColor(Color base, double saturation, double darkness) {
        Color adjusted = applyDarkness(applySaturation(base, saturation), darkness);
        return new Color(adjusted.getRed(), adjusted.getGreen(), adjusted.getBlue(), getAlpha255());
    }

    public Color getStyledGlobalColor() {
        return getStyledColor(globalColor.get(), globalSaturation.get(), globalDarskness.get());
    }

    public Color getStyledSecondColor() {
        return applyDarkness(getStyledGlobalColor(), 0.5);
    }
}
