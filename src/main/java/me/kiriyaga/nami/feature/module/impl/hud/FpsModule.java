package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.CAT_FORMAT;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class FpsModule extends HudElementModule {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("display label", true));

    public FpsModule() {
        super("fps", "Displays current FPS.", 52, 52, 50, 9);
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

        width = MC.textRenderer.getWidth(textStr);
        height = MC.textRenderer.fontHeight;

        if (displayLabel.get()) {
            return CAT_FORMAT.format("{bg}FPS: {bw}" + fps);
        } else {
            return Text.literal(textStr);
        }
    }
}