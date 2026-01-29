package me.kiriyaga.nami.impl.gui.oldgui.settings;

import me.kiriyaga.nami.impl.feature.impl.client.ColorFeature;
import me.kiriyaga.nami.impl.setting.impl.EnumSetting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.ColorUtils.*;

public class EnumSettingRenderer implements SettingRenderer<EnumSetting<?>> {

    @Override
    public void render(GuiGraphics context, Font textRenderer, EnumSetting<?> setting, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);
        Color textCol =  getColorFeature().getStyledTextColor(255);
        Color bgColor = new Color(30, 30, 30, 0);

        int bgColorInt = CLICK_GUI_SCREEN.applyFade(toRGBA(bgColor));
        int textColorInt = CLICK_GUI_SCREEN.applyFade(toRGBA(textCol));

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);

        int textX = x + PADDING + (hovered ? 1 : 0);
        int textY = y + (HEIGHT - 8) / 2;

        FONT_SERVICE.drawText(
                context,
                setting.getName(),
                textX,
                textY,
                textColorInt,
                true
        );

        String valueStr = setting.get().toString();
        FONT_SERVICE.drawText(
                context,
                valueStr,
                x + WIDTH - PADDING - FONT_SERVICE.getWidth(valueStr),
                textY,
                textColorInt,
                true
        );
    }

    @Override
    public boolean mouseClicked(EnumSetting<?> setting, double mouseX, double mouseY, int button) {
        if (button == 0) {
            setting.cycle(false);
        } else if (button == 1) {
            setting.cycle(true);
        }
        return true;
    }

    @Override
    public void mouseDragged(EnumSetting<?> setting, double mouseX) {
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    private ColorFeature getColorFeature() {
        return FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class);
    }

    private float approach(float current, float target, float maxDelta) {
        if (current < target) {
            current += maxDelta;
            if (current > target) current = target;
        } else if (current > target) {
            current -= maxDelta;
            if (current < target) current = target;
        }
        return current;
    }
}
