package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.CAT_FORMAT;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class FpsModule extends HudElementModule {

    public FpsModule() {
        super("fps", "Displays current FPS.", 52, 52, 50, 9);
    }

    @Override
    public Text getDisplayText() {
        int fps = MC.getCurrentFps();
        String textStr = "FPS: " + fps;
        width = MC.textRenderer.getWidth(textStr);
        height = MC.textRenderer.fontHeight;
        return CAT_FORMAT.format("{g}FPS: {reset}" + fps);
    }
}
