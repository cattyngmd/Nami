package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class WeatherModule extends HudElementModule {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("display label", true));

    public WeatherModule() {
        super("weather", "Displays current weather.", 0, 0, 50, 9);
    }

    @Override
    public Text getDisplayText() {
        if (MC.world == null) return CAT_FORMAT.format("{bg}NaN");

        String weather;

        if (MC.world.isRaining()) {
            if (MC.world.isThundering()) {
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

        width = MC.textRenderer.getWidth(text.replace("{bg}", "").replace("{bw}", ""));
        height = MC.textRenderer.fontHeight;

        return CAT_FORMAT.format(text);
    }
}
