package namidevelopment.kiriyaga.nami.impl.feature.hud;

import namidevelopment.kiriyaga.api.model.feature.HudElementFeature;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import net.minecraft.network.chat.Component;

import java.util.Locale;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;import static namidevelopment.kiriyaga.api.NamiApi.*;

@RegisterFeature
public class DirectionFeature extends HudElementFeature {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("Label", true));
    public final BoolSetting precise = addSetting(new BoolSetting("Precise", false));

    public DirectionFeature() {
        super("Direction", "Displays accurate yaw/pitch.", 0, 0, 50, 9);
    }

    @Override
    public Component getDisplayText() {
        String dirText;
        float yaw = 0;
        float pitch = 0;
        if (MC.getCameraEntity() != null) {
            yaw = MC.getCameraEntity().getYRot();
            pitch = MC.getCameraEntity().getXRot();
        }

        if (precise.get())
        dirText = String.format(Locale.US, "%.3f {g}[{w}%.3f{g}]", yaw, pitch); // locale is wild
        else
            dirText = String.format(Locale.US, "%.0f {g}[{w}%.0f{g}]", yaw, pitch); // locale is wild

        String text = displayLabel.get() ? "{g}Direction: {w}" + dirText : "{w}" + dirText;

        width = FONT_SERVICE.getWidth(text.replace("{g}", "").replace("{w}", ""));
        height = FONT_SERVICE.getHeight();

        return CAT_FORMAT.format(text);
    }
}