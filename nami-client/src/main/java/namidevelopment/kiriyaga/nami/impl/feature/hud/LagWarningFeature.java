package namidevelopment.kiriyaga.nami.impl.feature.hud;

import namidevelopment.kiriyaga.api.model.feature.HudElementFeature;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;
@RegisterFeature
public class LagWarningFeature extends HudElementFeature {


    public LagWarningFeature() {
        super("LagWarning", "Displays lag warning if connection unstable.", 0, 0, 100, 9);
    }

    @Override
    public Component getDisplayText() {
        if (!SERVER_SERVICE.isConnectionUnstable())
            return Component.empty();

        if (MC.isLocalServer() || MC.level == null) return Component.nullToEmpty("LagWarning:");

        double seconds = SERVER_SERVICE.getUnstableTime();
        double roundedSeconds = Math.round(seconds * 100.0) / 100.0;
        String warningText = "Server is not responding in " + String.format("%.2f", roundedSeconds) + "s";

        width = FONT_SERVICE.getWidth(warningText);
        height = FONT_SERVICE.getHeight();

        return CAT_FORMAT.format("{bg}" + warningText);
    }
}
