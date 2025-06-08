package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.ColorSetting;

import java.awt.*;

public class ColorModule extends Module {

    public final ColorSetting primaryColor = addSetting(new ColorSetting("Primary", new Color(100, 149, 237, 170), true));
    public final ColorSetting secondaryColor = addSetting(new ColorSetting("Secondary", new Color(70, 130, 180, 170), true));
    public final ColorSetting textColor = addSetting(new ColorSetting("Text", new Color(255, 255, 255, 170), true));

    public ColorModule() {
        super("Color", "Customizes color scheme", Category.CLIENT, "colr", "c", "colors", "clitor", "сщдщк");
    }
}
