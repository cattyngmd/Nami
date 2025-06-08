package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.ColorSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;

import java.awt.*;

public class ColorModule extends Module {

    public final ColorSetting primaryColor = addSetting(new ColorSetting("Primary", new Color(100, 149, 237, 170), true));
    public final ColorSetting secondaryColor = addSetting(new ColorSetting("Secondary", new Color(30, 30, 30, 170), true));
    public final ColorSetting textColor = addSetting(new ColorSetting("Text", new Color(170, 170, 170, 170), true));

    public final DoubleSetting primarySaturation = addSetting(new DoubleSetting("Primary Saturation", 0.7, 0.0, 1.0));
    public final DoubleSetting primaryDarkness = addSetting(new DoubleSetting("Primary Darkness", 0.0, 0.0, 1.0));

    public final DoubleSetting secondarySaturation = addSetting(new DoubleSetting("Secondary Saturation", 1.0, 0.0, 1.0));
    public final DoubleSetting secondaryDarkness = addSetting(new DoubleSetting("Secondary Darkness", 0.2, 0.0, 1.0));

    public final DoubleSetting textSaturation = addSetting(new DoubleSetting("Text Saturation", 1.0, 0.0, 1.0));
    public final DoubleSetting textDarkness = addSetting(new DoubleSetting("Text Darkness", 0.0, 0.0, 1.0));

    public ColorModule() {
        super("Color", "Customizes color scheme", Category.CLIENT, "colr", "c", "colors", "clitor", "сщдщк");
    }

    public Color applySaturation(Color base, double saturationFactor) {
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        float saturation = (float) (hsb[1] * saturationFactor);
        saturation = Math.max(0f, Math.min(1f, saturation));
        Color adjusted = Color.getHSBColor(hsb[0], saturation, hsb[2]);
        return new Color(adjusted.getRed(), adjusted.getGreen(), adjusted.getBlue(), base.getAlpha());
    }

    public Color applyDarkness(Color base, double darknessFactor) {
        float factor = (float) (1.0 - darknessFactor);
        int r = Math.max(0, (int) (base.getRed() * factor));
        int g = Math.max(0, (int) (base.getGreen() * factor));
        int b = Math.max(0, (int) (base.getBlue() * factor));
        return new Color(r, g, b, base.getAlpha());
    }

    public Color getStyledColor(Color base, double saturation, double darkness) {
        return applyDarkness(applySaturation(base, saturation), darkness);
    }

    public Color getStyledPrimaryColor() {
        return getStyledColor(primaryColor.get(), primarySaturation.get(), primaryDarkness.get());
    }

    public Color getStyledSecondaryColor() {
        return getStyledColor(secondaryColor.get(), secondarySaturation.get(), secondaryDarkness.get());
    }

    public Color getStyledTextColor() {
        return getStyledColor(textColor.get(), textSaturation.get(), textDarkness.get());
    }
}
