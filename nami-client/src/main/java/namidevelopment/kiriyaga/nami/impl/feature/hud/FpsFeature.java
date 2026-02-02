package namidevelopment.kiriyaga.nami.impl.feature.hud;

import namidevelopment.kiriyaga.nami.impl.feature.HudElementFeature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterFeature
public class FpsFeature extends HudElementFeature {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("Label", true));

    public FpsFeature() {
        super("FPS", "Displays current FPS.", 0, 0, 50, 9);
    }

    @Override
    public Component getDisplayText() {
        int fps = MC.getFps();
        String textStr;

        if (displayLabel.get()) {
            textStr = "FPS: " + fps;
        } else {
            textStr = String.valueOf(fps);
        }

        width = FONT_SERVICE.getWidth(textStr);
        height = FONT_SERVICE.getHeight();

        if (displayLabel.get()) {
            return CAT_FORMAT.format("{bg}FPS: {bw}" + fps);
        } else {
            return Component.literal(textStr);
        }
    }
}