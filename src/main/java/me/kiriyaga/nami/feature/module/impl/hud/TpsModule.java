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
    public final BoolSetting tpsSync = addSetting(new BoolSetting("sync with server tick", true));

    private static final int TPS_SAMPLES = 20;
    private final double[] tpsSamples = new double[TPS_SAMPLES];
    private int sampleIndex = 0;
    private boolean bufferFilled = false;

    public TpsModule() {
        super("tps", "Displays server TPS.", 52, 52, 50, 9);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTick(PreTickEvent event) {
        float tps = 20f;
        if (tpsSync.get() && MC.getServer() != null) {
            double tickTimeMs = MC.getServer().getAverageTickTime() / 1_000_000.0;
            tps = (float) Math.min(20.0, 1000.0 / tickTimeMs);
        }

        tpsSamples[sampleIndex] = tps;
        sampleIndex = (sampleIndex + 1) % TPS_SAMPLES;
        if (sampleIndex == 0) bufferFilled = true;
    }

    @Override
    public Text getDisplayText() {
        int count = bufferFilled ? TPS_SAMPLES : sampleIndex;
        if (count == 0) return CAT_FORMAT.format("{bg}TPS: {bw}NaN");

        double sum = 0;
        for (int i = 0; i < count; i++) {
            sum += tpsSamples[i];
        }
        double avgTps = sum / count;

        String tpsText = String.format("%.2f", avgTps);
        String text = displayLabel.get() ? "{bg}TPS: {bw}" + tpsText : "{bw}" + tpsText;

        width = MC.textRenderer.getWidth(text.replace("{bg}", "").replace("{bw}", ""));
        height = MC.textRenderer.fontHeight;

        return CAT_FORMAT.format(text);
    }
}