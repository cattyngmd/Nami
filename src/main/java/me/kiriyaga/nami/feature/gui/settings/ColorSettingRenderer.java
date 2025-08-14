package me.kiriyaga.nami.feature.gui.settings;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.setting.impl.ColorSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

import static me.kiriyaga.nami.Nami.CLICK_GUI;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.*;

public class ColorSettingRenderer implements SettingRenderer<ColorSetting> {
    private boolean dragging = false;
    private double startValue = 0;

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, ColorSetting setting, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);
        Color primary = getColorModule().getStyledGlobalColor();
        Color secondary = getColorModule().getStyledSecondColor();
        Color textCol = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).moduleFill.get()
                ? new Color(255, 255, 255, 255)
                : new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);
        Color bgColor = new Color(30, 30, 30, 0);

        float[] hsb = Color.RGBtoHSB(setting.getRed(), setting.getGreen(), setting.getBlue(), null);
        float hue = hsb[0];

        int bgColorInt = CLICK_GUI.applyFade(toRGBA(bgColor));
        int textColorInt = CLICK_GUI.applyFade(toRGBA(textCol));

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);

        String text = setting.getName();
        int textX = x + PADDING + (hovered ? 1 : 0);
        int textY = y + (HEIGHT - 8) / 2;

        context.drawText(textRenderer, text, textX, textY, textColorInt, false);

        renderHueSlider(context, x + PADDING, y + HEIGHT - 2, WIDTH - 2 * PADDING, SLIDER_HEIGHT, hue);

        int lineOffset = 1;
        if (MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).expandedIdentifier.get()) {
            context.fill(
                    x,
                    y - lineOffset,
                    x + 1,
                    y + HEIGHT,
                    CLICK_GUI.applyFade(
                            setting.getParentModule().isEnabled() ? primary.getRGB() : secondary.getRGB()
                    )
            );
        }

        String hex = String.format("#%02X%02X%02X", setting.getRed(), setting.getGreen(), setting.getBlue());
        context.drawText(
                textRenderer,
                hex,
                x + WIDTH - PADDING - textRenderer.getWidth(hex),
                textY,
                textColorInt,
                false
        );
    }

    @Override
    public boolean mouseClicked(ColorSetting setting, double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public void mouseDragged(ColorSetting setting, double deltaX) {
        float[] hsb = Color.RGBtoHSB(setting.getRed(), setting.getGreen(), setting.getBlue(), null);
        float newHue = (float) (hsb[0] + deltaX * 0.005);
        if (newHue > 1f) newHue -= 1f;
        if (newHue < 0f) newHue += 1f;
        Color newColor = Color.getHSBColor(newHue, 1f, 1f);
        setting.setValue(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), setting.getAlpha());
    }

    private void renderHueSlider(DrawContext context, int x, int y, int width, int height, float hue) {
        for (int i = 0; i < width; i++) {
            float h = i / (float) width;
            Color color = Color.getHSBColor(h, 1f, 1f);
            context.fill(x + i, y, x + i + 1, y + height, toRGBA(color));
        }
        int pos = (int) (hue * width);
        context.fill(x + pos - 1, y, x + pos + 1, y + height, toRGBA(Color.WHITE));
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
