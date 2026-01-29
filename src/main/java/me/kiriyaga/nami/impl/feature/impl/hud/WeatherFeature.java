package me.kiriyaga.nami.impl.feature.impl.hud;

import me.kiriyaga.nami.impl.feature.HudElementFeature;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import net.minecraft.network.chat.Component;

import static me.kiriyaga.nami.Nami.*;

@RegisterFeature
public class WeatherFeature extends HudElementFeature {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("Label", true));

    public WeatherFeature() {
        super("Weather", "Displays current weather.", 0, 0, 50, 9);
    }

    @Override
    public Component getDisplayText() {
        if (MC.level == null) return CAT_FORMAT.format("{bg}NaN");

        String weather;

        if (MC.level.isRaining()) {
            if (MC.level.isThundering()) {
                weather = "thunder";
            } else {
                weather = "rain";
            }
        } else {
            weather = "clear";
        }

        String text;
        if (displayLabel.get()) {
            text = "{bg}Weather: {bw}" + weather;
        } else {
            text = "{bw}" + weather;
        }

        width = FONT_SERVICE.getWidth(text.replace("{bg}", "").replace("{bw}", ""));
        height = FONT_SERVICE.getHeight();

        return CAT_FORMAT.format(text);
    }
}
