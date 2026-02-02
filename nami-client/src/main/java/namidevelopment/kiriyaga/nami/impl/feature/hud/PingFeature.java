package namidevelopment.kiriyaga.nami.impl.feature.hud;

import namidevelopment.kiriyaga.nami.impl.feature.HudElementFeature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import net.minecraft.network.chat.Component;

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
            return CAT_FORMAT.format("{bg}Ping: {bw}" + ping);
        } else {
            return Component.literal(textStr);
        }
    }
}
