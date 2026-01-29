package namidevelopment.kiriyaga.nami.impl.feature.impl.hud;

import namidevelopment.kiriyaga.nami.impl.feature.HudElementFeature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterFeature
public class WatermarkFeature extends HudElementFeature {
    public final BoolSetting plain = addSetting(new BoolSetting("Plain", false));

    public WatermarkFeature() {
        super("Watermark", "Displays client watermark.", 0, 0, 50, 9);
    }

    @Override
    public Component getDisplayText() {
        String watermarkStr = DISPLAY_NAME + " " + VERSION;
        if (watermarkStr.isEmpty()) {
            return CAT_FORMAT.format("{bg}NaN");
        }

        width = FONT_SERVICE.getWidth(watermarkStr);
        height = FONT_SERVICE.getHeight();

        return plain.get() ? CAT_FORMAT.format("{bg}" + DISPLAY_NAME + " "+ VERSION) : CAT_FORMAT.format("{bg}" + DISPLAY_NAME + " {bw}"+ VERSION);
    }
}
