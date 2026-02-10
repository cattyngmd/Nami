package namidevelopment.kiriyaga.nami.impl.feature.hud;

import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.feature.HudElementFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.api.model.setting.EnumSetting;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.api.NamiApi.*;

@RegisterFeature
public class FpsFeature extends HudElementFeature {
    public enum Mode { DEFAULT, INSTANT}

    public final BoolSetting displayLabel = addSetting(new BoolSetting("Label", true));
    public final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("Mode", Mode.DEFAULT));

    public FpsFeature() {
        super("FPS", "Displays current FPS.", 0, 0, 50, 9);
    }

    @Override
    public Component getDisplayText() {
        int fps;

        switch (mode.get()) {
            case INSTANT -> fps = SERVER_SERVICE.getInstantFPS();
            case DEFAULT -> fps = MC.getFps();
            default -> fps = MC.getFps();
        }

        String textStr;
        if (displayLabel.get()) {
            textStr = "FPS: " + fps;
        } else {
            textStr = String.valueOf(fps);
        }

        width = FONT_SERVICE.getWidth(textStr);
        height = FONT_SERVICE.getHeight();

        if (displayLabel.get()) {
            return CAT_FORMAT.format("{global}FPS: {white}" + fps);
        } else {
            return Component.literal(textStr);
        }
    }
}