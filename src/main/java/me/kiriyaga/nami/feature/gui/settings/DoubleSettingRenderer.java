package me.kiriyaga.nami.feature.gui.settings;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.*;

public class DoubleSettingRenderer implements SettingRenderer<DoubleSetting> {
    private double startValue = 0;
    private double accumulatedDeltaX = 0;

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, DoubleSetting setting, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);
        Color primary = getColorModule().getStyledGlobalColor();
        Color secondary = getColorModule().getStyledSecondColor();
        Color textCol = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).moduleFill.get()
                ? new Color(255, 255, 255, 255)
                : new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);
        Color bgColor = new Color(30, 30, 30, 0);

        int bgColorInt = CLICK_GUI.applyFade(toRGBA(bgColor));
        int textColorInt = CLICK_GUI.applyFade(toRGBA(textCol));

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);

        int textX = x + PADDING + (hovered ? 1 : 0);
        int textY = y + (HEIGHT - 8) / 2;

        FONT_MANAGER.drawText(
                context,
                setting.getName(),
                textX,
                textY,
                textColorInt,
                true
        );

        renderSlider(
                context,
                x + PADDING,
                y + HEIGHT - 2,
                WIDTH - 2 * PADDING,
                SLIDER_HEIGHT,
                setting.get(),
                setting.getMin(),
                setting.getMax(),
                primary
        );

        int lineOffset = 1;
        if (MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).expandedIdentifier.get())
            context.fill(
                    x,
                    y - lineOffset,
                    x + 1,
                    y + HEIGHT,
                    CLICK_GUI.applyFade(
                            textCol.getRGB()
                    )
            );

        double val = setting.get();
        double range = setting.getMax() - setting.getMin();
        String valStr = formatValue(val, range);

        FONT_MANAGER.drawText(
                context,
                valStr,
                x + WIDTH - PADDING - FONT_MANAGER.getWidth(valStr),
                textY,
                textColorInt,
                true
        );
    }

    @Override
    public boolean mouseClicked(DoubleSetting setting, double mouseX, double mouseY, int button) {
        startValue = setting.get();
        accumulatedDeltaX = 0;
        return true;
    }

    @Override
    public void mouseDragged(DoubleSetting setting, double deltaX) {
        double newValue = slideDouble(startValue, deltaX, setting);
        setting.set(newValue);
        startValue = newValue;
    }

    private double slideDouble(double start, double deltaX, DoubleSetting setting) {
        double min = setting.getMin();
        double max = setting.getMax();
        double range = max - min;
        double step = range / 15.0;
        double sensitivity = 0.02;

        accumulatedDeltaX += deltaX * sensitivity * range;

        if (Math.abs(accumulatedDeltaX) >= step) {
            double delta = accumulatedDeltaX;
            accumulatedDeltaX -= delta;
            double newValue = start + delta;
            newValue = Math.round(newValue / step) * step;
            return Math.max(min, Math.min(max, newValue));
        }

        return start;
    }

    private void renderSlider(DrawContext context, int x, int y, int width, int height,
                              double value, double min, double max, Color color) {
        context.fill(x, y, x + width, y + height, CLICK_GUI.applyFade(toRGBA(new Color(60, 60, 60, 150))));

        value = Math.max(min, Math.min(max, value));
        double percent = (value - min) / (max - min);
        int filledWidth = (int)(width * percent);

        context.fill(x, y, x + filledWidth, y + height, CLICK_GUI.applyFade(toRGBA(color)));
    }

    private String formatValue(double val, double range) {
        if (range <= 0.1) return String.format("%.3f", val);
        if (range <= 10) return String.format("%.2f", val);
        if (range <= 1000) return String.format("%.1f", val);
        return String.format("%.0f", val);
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    private ColorModule getColorModule() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
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
