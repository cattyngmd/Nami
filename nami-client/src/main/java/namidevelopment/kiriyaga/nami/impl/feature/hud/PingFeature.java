package namidevelopment.kiriyaga.nami.impl.feature.hud;

import namidevelopment.kiriyaga.api.model.feature.HudElementFeature;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.api.NamiApi.*;

@RegisterFeature
public class PingFeature extends HudElementFeature {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("Label", true));

    public PingFeature() {
        super("Ping", "Displays current Ping.", 0, 0, 50, 9);
    }

    @Override
    public Component getDisplayText() {
        int ping = SERVER_SERVICE.getPing();
        String textStr;

        if (displayLabel.get()) {
            textStr = "Ping: " + ping;
        } else {
            textStr = String.valueOf(ping);
        }

        width = FONT_SERVICE.getWidth(textStr);
        height = FONT_SERVICE.getHeight();

        if (displayLabel.get()) {
            return CAT_FORMAT.format("{global}Ping: {white}" + ping);
        } else {
            return Component.literal(textStr);
        }
    }
}