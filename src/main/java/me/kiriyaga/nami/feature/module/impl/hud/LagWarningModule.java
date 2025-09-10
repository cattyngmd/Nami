package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class LagWarningModule extends HudElementModule {


    public LagWarningModule() {
        super("lag warning", "Displays lag warning if connection unstable.", 0, 0, 100, 9);
    }

    @Override
    public Text getDisplayText() {
        if (!PING_MANAGER.isConnectionUnstable() || MC.isInSingleplayer()) return Text.empty();

        double seconds = PING_MANAGER.getConnectionUnstableTimeSeconds();
        double roundedSeconds = Math.round(seconds * 100.0) / 100.0;
        String warningText = "Server is not responding in " + String.format("%.2f", roundedSeconds) + "s";

        width = MC.textRenderer.getWidth(warningText);
        height = MC.textRenderer.fontHeight;

        return CAT_FORMAT.format("{bg}" + warningText);
    }
}
