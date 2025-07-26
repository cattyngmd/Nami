package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render2DEvent;
import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import me.kiriyaga.nami.util.ChatAnimationHelper;
import net.minecraft.text.Text;

import java.awt.*;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@RegisterModule
public class HudModule extends Module {

    public final BoolSetting chatAnimation = addSetting(new BoolSetting("chat animation", true));
    public final BoolSetting shadow = addSetting(new BoolSetting("shadow", true));
    public final BoolSetting bounce = addSetting(new BoolSetting("bounce", false));
    public final IntSetting bounceSpeed = addSetting(new IntSetting("bounce speed", 5, 1, 20));
    public final IntSetting bounceIntensity = addSetting(new IntSetting("bounce intensity", 30, 10, 100));

    private float bounceProgress = 0f;
    private boolean increasing = true;

    public HudModule() {
        super("hud", "Main HUD module", ModuleCategory.of("client"));
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        int screenHeight = MC.getWindow().getScaledHeight();
        int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();
        int chatZoneTop = screenHeight - (screenHeight / 8);

        for (Module module : MODULE_MANAGER.getStorage().getAll()) {
            if (module instanceof HudElementModule hudElement && hudElement.isEnabled()) {
                Text text = hudElement.getDisplayText();
                if (text == null) continue;

                int x = hudElement.getRenderX();
                int y = hudElement.getRenderY();
                int height = MC.textRenderer.fontHeight;

                boolean isInChatZone = (y + height) >= chatZoneTop;

                if (chatAnimation.get() && isInChatZone) {
                    y -= chatAnimationOffset;
                }

                event.getDrawContext().drawText(MC.textRenderer, text, x, y, 0xFFFFFFFF, shadow.get());
            }
        }
    }

    @SubscribeEvent
    public void onUpdate(PreTickEvent event) {
        if (bounce.get()) {
            float step = bounceSpeed.get() / 100f;
            if (increasing) {
                bounceProgress += step;
                if (bounceProgress >= 1f) {
                    bounceProgress = 1f;
                    increasing = false;
                }
            } else {
                bounceProgress -= step;
                if (bounceProgress <= 0f) {
                    bounceProgress = 0f;
                    increasing = true;
                }
            }
        } else {
            bounceProgress = 0f;
        }
    }

    public Color getPulsingColor(Color originalColor) {
        if (!bounce.get()) return originalColor;

        float intensity = bounceIntensity.get() / 100f;
        float pulseFactor = (float) Math.sin(bounceProgress * Math.PI);

        int a = originalColor.getAlpha();
        int r = originalColor.getRed();
        int g = originalColor.getGreen();
        int b = originalColor.getBlue();

        int minAlpha = (int) (a * (1 - intensity));
        int pulsingAlpha = minAlpha + (int) ((a - minAlpha) * pulseFactor);

        return new Color(r, g, b, pulsingAlpha);
    }
}