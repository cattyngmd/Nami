package me.kiriyaga.nami.impl.feature.impl.hud;

import me.kiriyaga.nami.impl.feature.HudElementFeature;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import net.minecraft.network.chat.Component;

import java.util.Locale;

import static me.kiriyaga.nami.Nami.*;

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
        dirText = String.format(Locale.US, "%.3f {bg}[{bw}%.3f{bg}]", yaw, pitch); // locale is wild
        else
            dirText = String.format(Locale.US, "%.0f {bg}[{bw}%.0f{bg}]", yaw, pitch); // locale is wild

        String text = displayLabel.get() ? "{bg}Direction: {bw}" + dirText : "{bw}" + dirText;

        width = FONT_SERVICE.getWidth(text.replace("{bg}", "").replace("{bw}", ""));
        height = FONT_SERVICE.getHeight();

        return CAT_FORMAT.format(text);
    }
}