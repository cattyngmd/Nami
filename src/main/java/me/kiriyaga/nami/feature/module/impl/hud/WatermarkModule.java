package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.CAT_FORMAT;
import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.DISPLAY_NAME;
import static me.kiriyaga.nami.Nami.VERSION;

@RegisterModule
public class WatermarkModule extends HudElementModule {

    public WatermarkModule() {
        super("watermark", "Displays client watermark.", 0, 0, 50, 9);
    }

    @Override
    public Text getDisplayText() {
        String watermarkStr = DISPLAY_NAME + " " + VERSION;
        if (watermarkStr.isEmpty()) {
            return CAT_FORMAT.format("{bg}NaN");
        }

        width = MC.textRenderer.getWidth(watermarkStr);
        height = MC.textRenderer.fontHeight;

        return CAT_FORMAT.format("{bg}" + DISPLAY_NAME +" "+ VERSION);
    }
}
