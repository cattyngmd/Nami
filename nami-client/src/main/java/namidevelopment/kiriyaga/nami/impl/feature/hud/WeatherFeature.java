package namidevelopment.kiriyaga.nami.impl.feature.hud;

import namidevelopment.kiriyaga.api.model.feature.HudElementFeature;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;
@RegisterFeature
public class WeatherFeature extends HudElementFeature {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("Label", true));

    public WeatherFeature() {
        super("Weather", "Displays current weather.", 0, 0, 50, 9);
    }

    @Override
    public Component getDisplayText() {
        if (MC.level == null) return CAT_FORMAT.format("{global}NaN");

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
            text = "{global}Weather: {white}" + weather;
        } else {
            text = "{white}" + weather;
        }

        width = FONT_SERVICE.getWidth(text.replace("{global}", "").replace("{white}", ""));
        height = FONT_SERVICE.getHeight();

        return CAT_FORMAT.format(text);
    }
}
