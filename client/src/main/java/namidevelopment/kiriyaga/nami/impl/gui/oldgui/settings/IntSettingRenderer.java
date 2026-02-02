package namidevelopment.kiriyaga.nami.impl.gui.oldgui.settings;

import namidevelopment.kiriyaga.nami.impl.feature.impl.client.ColorFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

import static namidevelopment.kiriyaga.nami.Nami.*;

public class IntSettingRenderer implements SettingRenderer<IntSetting> {
    private boolean dragging = false;

    private int lastSliderX, lastSliderY, lastSliderWidth, lastSliderHeight;

    @Override
    public void render(GuiGraphics context, Font textRenderer, IntSetting setting, int x, int y, int mouseX, int mouseY) {

        boolean hovered = isHovered(mouseX, mouseY, x, y);

        Color primary = getColorFeature().getStyledGlobalColor();
        Color text = getColorFeature().getStyledTextColor(255);

        int bgColorInt = CLICK_GUI_SCREEN.applyFade(toRGBA(new Color(30, 30, 30, 0)));
        int textColorInt = CLICK_GUI_SCREEN.applyFade(toRGBA(text));

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);

        int textX = x + PADDING + (hovered ? 1 : 0);
        int textY = y + (HEIGHT - 8) / 2;
        FONT_SERVICE.drawText(context, setting.getName(), textX, textY, textColorInt, true);

        lastSliderX = x + PADDING;
        lastSliderY = y + HEIGHT - 2;
        lastSliderWidth = WIDTH - 2 * PADDING;
        lastSliderHeight = SLIDER_HEIGHT;

        renderSlider(context, lastSliderX, lastSliderY, lastSliderWidth, lastSliderHeight,
                setting.get(), setting.getMin(), setting.getMax(), primary);

        String valStr = String.valueOf(setting.get());
        FONT_SERVICE.drawText(context, valStr,
                x + WIDTH - PADDING - FONT_SERVICE.getWidth(valStr),
                textY, textColorInt, true);
    }

    @Override
    public boolean mouseClicked(IntSetting setting, double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        if (mouseX >= lastSliderX - PADDING && mouseX <= lastSliderX + lastSliderWidth + PADDING &&
                mouseY >= lastSliderY - HEIGHT && mouseY <= lastSliderY + lastSliderHeight + HEIGHT) {

            updateFromMouse(setting, mouseX);
            dragging = true;
            return true;
        }

        return false;
    }


    public void updateMouseDrag(IntSetting setting, double mouseX, double mouseY) {
        if (!dragging) return;
        updateFromMouse(setting, mouseX);
    }

    private void updateFromMouse(IntSetting setting, double mouseX) {
        int min = setting.getMin();
        int max = setting.getMax();
        int range = Math.max(1, max - min);

        double percent = (mouseX - lastSliderX) / (double) lastSliderWidth;
        percent = Math.max(0.0, Math.min(1.0, percent));

        int newValue = (int) Math.round(min + percent * range);
        newValue = Math.max(min, Math.min(max, newValue));
        setting.set(newValue);
    }

    @Override
    public boolean mouseReleased(IntSetting setting, double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        boolean was = dragging;
        dragging = false;
        return was;
    }

    private void renderSlider(GuiGraphics context, int x, int y, int width, int height,
                              int value, int min, int max, Color color) {
        context.fill(x, y, x + width, y + height, CLICK_GUI_SCREEN.applyFade(toRGBA(new Color(60, 60, 60, 150))));

        value = Math.max(min, Math.min(max, value));
        double percent = (value - min) / (double) Math.max(1, (max - min));
        int filledWidth = (int) (width * percent);

        context.fill(x, y, x + filledWidth, y + height, CLICK_GUI_SCREEN.applyFade(toRGBA(color)));
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    private ColorFeature getColorFeature() {
        return FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class);
    }

    private static int toRGBA(Color c) {
        return (c.getAlpha() & 0xFF) << 24 |
                (c.getRed() & 0xFF) << 16 |
                (c.getGreen() & 0xFF) << 8 |
                (c.getBlue() & 0xFF);
    }
}
