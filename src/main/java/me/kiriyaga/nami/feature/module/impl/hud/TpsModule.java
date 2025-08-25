package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;

import me.kiriyaga.nami.event.SubscribeEvent;

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
        String tpsText = String.format("%.2f {bg}[{bw}%.2f{bg}]", avgTps, latestTps);
        String text = displayLabel.get() ? "{bg}TPS: {bw}" + tpsText : "{bw}" + tpsText;

        width = MC.textRenderer.getWidth(text.replace("{bg}", "").replace("{bw}", ""));
        height = MC.textRenderer.fontHeight;

        return CAT_FORMAT.format(text);
    }
}