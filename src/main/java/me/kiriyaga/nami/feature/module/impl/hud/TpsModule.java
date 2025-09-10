package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import net.minecraft.text.Text;

import java.util.Locale;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class TpsModule extends HudElementModule {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("display label", true));

    public TpsModule() {
        super("tps", "Displays server TPS.", 0, 0, 50, 9);
    }

    @Override
    public Text getDisplayText() {
        float avgTps = TICK_MANAGER.getAverageTPS();
        float latestTps = TICK_MANAGER.getLatestTPS();
        String tpsText = String.format(Locale.US, "%.2f {bg}[{bw}%.2f{bg}]", avgTps, latestTps); // locale is wild
        String text = displayLabel.get() ? "{bg}TPS: {bw}" + tpsText : "{bw}" + tpsText;

        width = MC.textRenderer.getWidth(text.replace("{bg}", "").replace("{bw}", ""));
        height = MC.textRenderer.fontHeight;

        return CAT_FORMAT.format(text);
    }
}