package me.kiriyaga.nami.util;

import dev.cattyn.catformat.stylist.annotations.Style;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.impl.client.HudModule;

import java.awt.*;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class CatStyles {

    @Style("g")
    Color global() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor();
    }

    @Style("s")
    Color secondary() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledSecondColor();
    }

    @Style("namiRed")
    final Color namiRed() {
        return new Color(180, 0, 0);
    }

    @Style("namiDarkRed")
    final Color namiDarkRed() {
        return new Color(110, 0, 0);
    }

    @Style("bg")
    Color bounceGlobal() {
        return MODULE_MANAGER.getStorage().getByClass(HudModule.class).getPulsingColor(MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor());
    }

    @Style("bw")
    Color bounceWhite() {
        return MODULE_MANAGER.getStorage().getByClass(HudModule.class).getPulsingColor(new Color(255, 255,255, 255));
    }
}
