package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.ColorSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;

import java.awt.*;

public class ColorModule extends Module {

    public final ColorSetting globalColor = addSetting(new ColorSetting("global", new Color(255, 0, 248, 170), true));

    public final DoubleSetting globalSaturation = addSetting(new DoubleSetting("saturation", 0.45, 0.0, 1.0));
    public final DoubleSetting globalDarskness = addSetting(new DoubleSetting("darkness", 0.05, 0.0, 1.0));

    public final DoubleSetting alpha = addSetting(new DoubleSetting("alpha", 0.7, 0.0, 1.0));

    public final BoolSetting rainbowEnabled = addSetting(new BoolSetting("rainbow", false));
    public final DoubleSetting rainbowSpeed = addSetting(new DoubleSetting("rainbowSpeed", 0.5, 0.01, 5.0));

    public ColorModule() {
        super("color", "Customizes color scheme.", Category.client, "colr", "c", "colors", "clitor", "сщдщк");
        if (!this.isEnabled())
            this.toggle();
    }

    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
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
        int a = base.getAlpha();
        return new Color(r, g, b, a);
    }

    public Color getStyledColor(Color base, double saturation, double darkness) {
        Color adjusted = applyDarkness(applySaturation(base, saturation), darkness);
        return new Color(adjusted.getRed(), adjusted.getGreen(), adjusted.getBlue(), getAlpha255());
    }

    private Color getRainbowColor() {
        long time = System.currentTimeMillis();
        float hue = (time * 0.001f * rainbowSpeed.get().floatValue()) % 1.0f;
        Color rainbow = Color.getHSBColor(hue, 1f, 1f);
        return new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), getAlpha255());
    }

    public Color getEffectiveGlobalColor() {
        if (rainbowEnabled.get()) {
            return getRainbowColor();
        }
        return globalColor.get();
    }

    public Color getStyledGlobalColor() {
        return getStyledColor(getEffectiveGlobalColor(), globalSaturation.get(), globalDarskness.get());
    }

    public Color getStyledSecondColor() {
        return applyDarkness(getStyledGlobalColor(), 0.5);
    }
}
