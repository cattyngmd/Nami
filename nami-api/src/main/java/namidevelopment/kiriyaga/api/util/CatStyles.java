package namidevelopment.kiriyaga.api.util;

import dev.cattyn.catformat.stylist.annotations.Style;
import namidevelopment.kiriyaga.api.contract.FeatureContractService;
import namidevelopment.kiriyaga.api.contract.feature.ColorFeatureConfig;

import static namidevelopment.kiriyaga.api.NamiApi.*;


import java.awt.*;

public class CatStyles {

    @Style("g")
    Color global() {
        Color gc = FeatureContractService.get(ColorFeatureConfig.class).getStyledGlobalColor();
        return new Color(gc.getRed(), gc.getGreen(), gc.getBlue(), 255);
    }

    @Style("friend")
    Color friend() {
        Color gc = FeatureContractService.get(ColorFeatureConfig.class).getFriendColor();
        return new Color(gc.getRed(), gc.getGreen(), gc.getBlue(), 255);
    }

    @Style("s")
    Color secondary() {
        Color gs = FeatureContractService.get(ColorFeatureConfig.class).getStyledSecondColor();
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

    @Style("w")
    Color whitee() {
        return new Color(255, 255,255);
    }

    @Style("gray")
    Color grayy() {
        return new Color(77,77,77);
    }
}
