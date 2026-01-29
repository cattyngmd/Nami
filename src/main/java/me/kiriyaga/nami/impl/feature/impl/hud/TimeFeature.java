package me.kiriyaga.nami.impl.feature.impl.hud;

import me.kiriyaga.nami.impl.feature.HudElementFeature;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.EnumSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static me.kiriyaga.nami.Nami.*;

@RegisterFeature
public class TimeFeature extends HudElementFeature {

    public enum TimeMode {
        REAL,
        GAME
    }

    public final EnumSetting<TimeMode> mode = addSetting(new EnumSetting<>("Mode", TimeMode.REAL));
    public final BoolSetting grey = addSetting(new BoolSetting("Grey", true));

    public TimeFeature() {
        super("Time", "Displays real or game time.", 0, 0, 50, 9);
    }

    @Override
    public Component getDisplayText() {
        Minecraft mc = MC;
        if (mc.level == null) return CAT_FORMAT.format("{bg}NaN");

        String timeText;

        if (mode.get() == TimeMode.REAL) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            timeText = now.format(formatter);
        } else {
            long time = mc.level.getDayTime() % 24000;
            int minecraftHour = (int)((time / 1000 + 6) % 24);
            int minecraftMinute = (int)((time % 1000) * 60 / 1000);

            timeText = String.format("%02d:%02d", minecraftHour, minecraftMinute);
        }

        width = FONT_SERVICE.getWidth(timeText);
        height = FONT_SERVICE.getHeight();

        if (!grey.get())
            return CAT_FORMAT.format("{bg}" + timeText);
        else
            return CAT_FORMAT.format("{bgr}" + timeText);
    }
}
