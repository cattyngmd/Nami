package me.kiriyaga.nami.feature.gui.settings;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.*;

public class IntSettingRenderer implements SettingRenderer<IntSetting> {
    private double accumulatedDeltaX = 0;

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, IntSetting setting, int x, int y, int mouseX, int mouseY) {
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

        FONT_MANAGER.drawText(context, setting.getName(), textX, textY, textColorInt, true);

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

        String valStr = String.valueOf(setting.get());
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
    public boolean mouseClicked(IntSetting setting, double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public void mouseDragged(IntSetting setting, double deltaX) {
        int newValue = slideInt(setting.get(), deltaX, setting);
        setting.set(newValue);
    }

    private void renderSlider(DrawContext context, int x, int y, int width, int height,
                              int value, int min, int max, Color color) {
        context.fill(x, y, x + width, y + height, CLICK_GUI.applyFade(toRGBA(new Color(60, 60, 60, 150))));

        value = Math.max(min, Math.min(max, value));
        double percent = (value - min) / (double)(max - min);
        int filledWidth = (int)(width * percent);

        context.fill(x, y, x + filledWidth, y + height, CLICK_GUI.applyFade(toRGBA(color)));
    }

    private int slideInt(int start, double deltaX, IntSetting setting) {
        int min = setting.getMin();
        int max = setting.getMax();
        int range = max - min;
        int step = Math.max(1, range / 15);
        double sensitivity = 0.02;

        accumulatedDeltaX += deltaX * sensitivity * range;

        if (Math.abs(accumulatedDeltaX) >= step) {
            int delta = (int) Math.round(accumulatedDeltaX);
            accumulatedDeltaX -= delta;
            int newValue = start + delta;
            newValue = Math.round((float) newValue / step) * step;
            return Math.max(min, Math.min(max, newValue));
        }

        return start;
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

    private static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    private ColorModule getColorModule() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
    }
}
