package namidevelopment.kiriyaga.nami.impl.feature.hud;

import namidevelopment.kiriyaga.api.model.feature.HudElementFeature;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;
@RegisterFeature
public class SetbackFeature extends HudElementFeature {


    public SetbackFeature() {
        super("Setback", "Displays setback warning once server cancelled any of your packets.", 0, 0, 100, 9);
    }

    @Override
    public Component getDisplayText() {

        if (SERVER_SERVICE.hasElapsedSinceSetback(5000))
            return Component.empty();

        if (MC.isLocalServer() || MC.level == null) return Component.nullToEmpty("Setback:");

        long last = SERVER_SERVICE.getLastSetbackTime();
        double delta = (System.currentTimeMillis() - last) / 1000.0;
        double rounded = Math.round(delta * 100.0) / 100.0;
        String warningText = "Setback was: " + String.format("%.2f", rounded) + "s";

        width = FONT_SERVICE.getWidth(warningText);
        height = FONT_SERVICE.getHeight();

        return CAT_FORMAT.format("{g}" + warningText);
    }
}
