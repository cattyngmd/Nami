package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.MC;

//@RegisterModule
public class TestPositionModule extends HudElementModule {

    private boolean toggle = false;
    private long lastToggleTime = 0;

    public TestPositionModule() {
        super("testpos", ".", 0, 0, 50, 9);
    }

    @Override
    public Text getDisplayText() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastToggleTime >= 5000) {
            toggle = !toggle;
            lastToggleTime = currentTime;
        }

        String textStr = toggle ? "9999999" : "9";

        width = MC.textRenderer.getWidth(textStr);
        height = MC.textRenderer.fontHeight;

        return Text.literal(textStr);
    }
}
