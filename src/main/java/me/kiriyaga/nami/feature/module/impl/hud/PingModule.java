package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class PingModule extends HudElementModule {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("display label", true));

    public PingModule() {
        super("ping", "Displays current Ping.", 0, 0, 50, 9);
    }

    @Override
    public Text getDisplayText() {
        int ping = PING_MANAGER.getPing();
        String textStr;

        if (displayLabel.get()) {
            textStr = "Ping: " + ping;
        } else {
            textStr = String.valueOf(ping);
        }

        width = MC.textRenderer.getWidth(textStr);
        height = MC.textRenderer.fontHeight;

        if (displayLabel.get()) {
            return CAT_FORMAT.format("{bg}Ping: {bw}" + ping);
        } else {
            return Text.literal(textStr);
        }
    }
}
