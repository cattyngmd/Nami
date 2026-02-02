package namidevelopment.kiriyaga.nami.impl.feature.hud;

import namidevelopment.kiriyaga.nami.impl.feature.HudElementFeature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import net.minecraft.network.chat.Component;

import java.util.Locale;

@RegisterFeature
public class TpsFeature extends HudElementFeature {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("Label", true));

    public TpsFeature() {
        super("TPS", "Displays server TPS.", 0, 0, 50, 9);
    }

    @Override
    public Component getDisplayText() {
        float avgTps = SERVER_SERVICE.getAverageTPS();
        float latestTps = SERVER_SERVICE.getLatestTPS();
        String tpsText = String.format(Locale.US, "%.2f {bg}[{bw}%.2f{bg}]", avgTps, latestTps); // locale is wild
        String text = displayLabel.get() ? "{bg}TPS: {bw}" + tpsText : "{bw}" + tpsText;

        width = FONT_SERVICE.getWidth(text.replace("{bg}", "").replace("{bw}", ""));
        height = FONT_SERVICE.getHeight();

        return CAT_FORMAT.format(text);
    }
}