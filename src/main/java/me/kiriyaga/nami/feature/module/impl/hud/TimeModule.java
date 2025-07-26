package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class TimeModule extends HudElementModule {

    public enum TimeMode {
        real,
        game
    }

    public final EnumSetting<TimeMode> mode = addSetting(new EnumSetting<>("mode", TimeMode.real));

    public TimeModule() {
        super("time", "Displays real or game time.", 52, 52, 50, 9);
    }

    @Override
    public Text getDisplayText() {
        MinecraftClient mc = MC;
        if (mc.world == null) return CAT_FORMAT.format("{bg}NaN");

        String timeText;

        if (mode.get() == TimeMode.real) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            timeText = now.format(formatter);
        } else {
            long time = mc.world.getTimeOfDay() % 24000;
            int minecraftHour = (int)((time / 1000 + 6) % 24);
            int minecraftMinute = (int)((time % 1000) * 60 / 1000);

            timeText = String.format("%02d:%02d", minecraftHour, minecraftMinute);
        }

        width = MC.textRenderer.getWidth(timeText);
        height = MC.textRenderer.fontHeight;

        return CAT_FORMAT.format("{bg}" + timeText);
    }
}
