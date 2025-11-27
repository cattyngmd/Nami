package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class UptimeModule extends HudElementModule {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("Label", true));
    public final BoolSetting days = addSetting(new BoolSetting("Days", true));
    public final BoolSetting hours = addSetting(new BoolSetting("Hours", true));
    public final BoolSetting minutes = addSetting(new BoolSetting("Minutes", true));
    public final BoolSetting seconds = addSetting(new BoolSetting("Seconds", true));

    public UptimeModule() {
        super("Uptime", "Displays total time of minecraft run time.", 0, 0, 120, 10);
    }

    @Override
    public Text getDisplayText() {
        long uptimeMillis = System.currentTimeMillis() - START_TIME;
        long uptimeSec = uptimeMillis / 1000;

        long days = uptimeSec / 86400;
        long hours = (uptimeSec % 86400) / 3600;
        long minutes = (uptimeSec % 3600) / 60;
        long seconds = uptimeSec % 60;

        StringBuilder sb = new StringBuilder();

        if (displayLabel.get())
            sb.append("{bg}Uptime: {bw}");

        boolean first = true;

        if (this.days.get()) {
            sb.append(days).append(" ").append("days{bg},{bw}");
            first = false;
        }

        if (this.hours.get()) {
            if (!first) sb.append(" ");
            sb.append(hours).append(" hours{bg},{bw}");
            first = false;
        }

        if (this.minutes.get()) {
            if (!first) sb.append(" ");
            sb.append(minutes).append(" minutes{bg},{bw}");
            first = false;
        }

        if (this.seconds.get()) {
            if (!first) sb.append(" ");
            sb.append(seconds).append(" seconds");
        }

        String formatted = sb.toString();
        width = FONT_MANAGER.getWidth(formatted.replace("{bg}", "").replace("{bw}", ""));
        height = FONT_MANAGER.getHeight();

        return CAT_FORMAT.format(formatted);
    }
}
