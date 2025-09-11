package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class FpsModule extends HudElementModule {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("display label", true));

    public FpsModule() {
        super("fps", "Displays current FPS.", 0, 0, 50, 9);
    }

    @Override
    public Text getDisplayText() {
        int fps = MC.getCurrentFps();
        String textStr;

        if (displayLabel.get()) {
            textStr = "FPS: " + fps;
        } else {
            textStr = String.valueOf(fps);
        }

        width = FONT_MANAGER.getWidth(textStr);
        height = FONT_MANAGER.getHeight();

        if (displayLabel.get()) {
            return CAT_FORMAT.format("{bg}FPS: {bw}" + fps);
        } else {
            return Text.literal(textStr);
        }
    }
}