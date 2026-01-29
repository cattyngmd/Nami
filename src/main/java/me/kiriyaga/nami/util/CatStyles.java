package me.kiriyaga.nami.util;

import dev.cattyn.catformat.stylist.annotations.Style;
import me.kiriyaga.nami.impl.feature.impl.client.ColorFeature;
import me.kiriyaga.nami.impl.feature.impl.client.HudFeature;

import java.awt.*;

import static me.kiriyaga.nami.Nami.FEATURE_SERVICE;

public class CatStyles {

    @Style("g")
    Color global() {
        Color gc = FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class).getStyledGlobalColor();
        return new Color(gc.getRed(), gc.getGreen(), gc.getBlue(), 255);
    }

    @Style("friend")
    Color friend() {
        Color gc = FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class).getFriendColor();
        return new Color(gc.getRed(), gc.getGreen(), gc.getBlue(), 255);
    }

    @Style("s")
    Color secondary() {
        Color gs = FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class).getStyledSecondColor();
        return new Color(gs.getRed(), gs.getGreen(), gs.getBlue(), 255);
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
        Color gc = FEATURE_SERVICE.getStorage().getByClass(HudFeature.class).accent.get() ? FEATURE_SERVICE.getStorage().getByClass(HudFeature.class).globalColor.get() : FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class).getStyledGlobalColor();

        return FEATURE_SERVICE.getStorage().getByClass(HudFeature.class).getPulsingColor(new Color(gc.getRed(), gc.getGreen(), gc.getBlue(), 255));
    }

    @Style("bf")
    Color bounceFriend() {
        Color gc = FEATURE_SERVICE.getStorage().getByClass(HudFeature.class).accent.get() ? FEATURE_SERVICE.getStorage().getByClass(HudFeature.class).globalColor.get() : FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class).getFriendColor();

        return FEATURE_SERVICE.getStorage().getByClass(HudFeature.class).getPulsingColor(new Color(gc.getRed(), gc.getGreen(), gc.getBlue(), 255));
    }

    @Style("bw")
    Color bounceWhite() {
        return FEATURE_SERVICE.getStorage().getByClass(HudFeature.class).getPulsingColor(new Color(255, 255,255));
    }

    @Style("bgr")
    Color bounceGray() {
        return FEATURE_SERVICE.getStorage().getByClass(HudFeature.class).getPulsingColor(new Color(77,77,77));
    }
}
