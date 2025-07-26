package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class PingModule extends HudElementModule {

    public PingModule() {
        super("ping", "Displays current Ping.", 52, 52, 50, 9);
    }

    @Override
    public Text getDisplayText() {
        int ping = PING_MANAGER.getPing();
        String textStr = "Ping: " + ping;
        width = MC.textRenderer.getWidth(textStr);
        height = MC.textRenderer.fontHeight;
        return CAT_FORMAT.format("{g}Ping: {reset}" + ping);
    }
}
