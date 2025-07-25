package me.kiriyaga.nami.util;

import dev.cattyn.catformat.stylist.annotations.Style;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;

import java.awt.*;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class CatStylesUtil {

    @Style("global")
    public static int global() {
        ColorModule cm = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
        return cm.getStyledGlobalColor().getRGB() & 0xFFFFFF;
    }

    @Style("secondary")
    public static int secondary() {
        ColorModule cm = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
        return cm.getStyledGlobalColor().getRGB() & 0xFFFFFF;
    }
}
