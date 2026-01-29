package namidevelopment.kiriyaga.nami.impl.gui.oldgui.settings;

import namidevelopment.kiriyaga.nami.impl.feature.impl.client.ColorFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.ColorSetting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

import static namidevelopment.kiriyaga.nami.Nami.*;

public class ColorSettingRenderer implements SettingRenderer<ColorSetting> {
    private boolean draggingHue = false;
    private boolean draggingSV = false;

    private static final int SV_SIZE = WIDTH - PADDING * 2;
    private static final int HUE_HEIGHT = SLIDER_HEIGHT;
    private static final int RENDER_STEP = 2; // i mean yeah we can render sv image instead but whatever its blockgame cheat
    private static final int HUE_CLICK_PADDING = 4;

    private static final int SV_HUE_PADDING = 6;
    private static final int BOTTOM_PADDING = 6;

    private int lastSvX, lastSvY;
    private int lastHueX, lastHueY;

    @Override
    public void render(GuiGraphics context, Font textRenderer, ColorSetting setting,
                       int x, int y, int mouseX, int mouseY) {

        boolean hovered = isHovered(mouseX, mouseY, x, y);
        Color textCol = getColorFeature().getStyledTextColor(255);

        int bgColorInt = CLICK_GUI_SCREEN.applyFade(toRGBA(new Color(30, 30, 30, 0)));
        int textColorInt = CLICK_GUI_SCREEN.applyFade(toRGBA(textCol));

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);

        int textX = x + PADDING + (hovered ? 1 : 0);
        int textY = y + (HEIGHT - 8) / 2;
        FONT_SERVICE.drawText(context, setting.getName(), textX, textY, textColorInt, true);

        lastSvX = x + PADDING;
        lastSvY = y + HEIGHT;
        renderSVSquare(context, lastSvX, lastSvY, SV_SIZE, SV_SIZE, setting);

        lastHueX = lastSvX;
        lastHueY = lastSvY + SV_SIZE + SV_HUE_PADDING + 3;
        renderHueSlider(context, lastHueX, lastHueY, SV_SIZE, HUE_HEIGHT, setting);

        String hex = String.format("#%02X%02X%02X", setting.getRed(), setting.getGreen(), setting.getBlue());
        FONT_SERVICE.drawText(
                context,
                hex,
                x + WIDTH - PADDING - FONT_SERVICE.getWidth(hex),
                textY,
                textColorInt,
                true
        );
    }

    @Override
    public int getHeight(ColorSetting setting) {
        return HEIGHT + SV_SIZE + SV_HUE_PADDING + HUE_HEIGHT + BOTTOM_PADDING;
    }

    private void renderSVSquare(GuiGraphics context, int x, int y, int w, int h, ColorSetting setting) {
        float[] hsb = Color.RGBtoHSB(setting.getRed(), setting.getGreen(), setting.getBlue(), null);
        float hue = hsb[0];

        for (int i = 0; i < w; i += RENDER_STEP) {
            for (int j = 0; j < h; j += RENDER_STEP) {
                float sat = i / (float) w;
                float bri = 1f - j / (float) h;
                Color c = Color.getHSBColor(hue, sat, bri);
                context.fill(x + i, y + j, x + i + RENDER_STEP, y + j + RENDER_STEP, CLICK_GUI_SCREEN.applyFade(toRGBA(c)));
            }
        }

        int cursorX = (int) (hsb[1] * w);
        int cursorY = (int) ((1 - hsb[2]) * h);
        context.fill(x + cursorX - 2, y + cursorY - 2,
                x + cursorX + 2, y + cursorY + 2,
                CLICK_GUI_SCREEN.applyFade(toRGBA(Color.WHITE)));
    }

    private void renderHueSlider(GuiGraphics context, int x, int y, int width, int height, ColorSetting setting) {
        float[] hsb = Color.RGBtoHSB(setting.getRed(), setting.getGreen(), setting.getBlue(), null);

        for (int i = 0; i < width; i++) {
            float h = i / (float) width;
            Color c = Color.getHSBColor(h, 1f, 1f);
            context.fill(x + i, y, x + i + 1, y + height, CLICK_GUI_SCREEN.applyFade(toRGBA(c)));
        }

        int huePos = (int) (hsb[0] * width);
        context.fill(x + huePos - 2, y - 1, x + huePos + 2, y + height + 1, CLICK_GUI_SCREEN.applyFade(toRGBA(Color.WHITE)));
    }

    @Override
    public boolean mouseClicked(ColorSetting setting, double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        if (mouseX >= lastHueX && mouseX <= lastHueX + SV_SIZE &&
                mouseY >= lastHueY - HUE_CLICK_PADDING && mouseY <= lastHueY + HUE_HEIGHT + HUE_CLICK_PADDING) {
            updateHue(setting, mouseX);
            draggingHue = true;
            return true;
        }

        if (mouseX >= lastSvX && mouseX <= lastSvX + SV_SIZE &&
                mouseY >= lastSvY && mouseY <= lastSvY + SV_SIZE) {
            updateSV(setting, mouseX, mouseY);
            draggingSV = true;
            return true;
        }

        return false;
    }

    public void updateMouseDrag(ColorSetting setting, double mouseX, double mouseY) {
        if (draggingSV) updateSV(setting, mouseX, mouseY);
        else if (draggingHue) updateHue(setting, mouseX);
    }

    private void updateSV(ColorSetting setting, double mouseX, double mouseY) {
        float[] hsb = Color.RGBtoHSB(setting.getRed(), setting.getGreen(), setting.getBlue(), null);
        float sat = (float)((mouseX - lastSvX) / (float)SV_SIZE);
        float bri = 1f - (float)((mouseY - lastSvY) / (float)SV_SIZE);
        sat = Math.max(0f, Math.min(1f, sat));
        bri = Math.max(0f, Math.min(1f, bri));

        int rgb = Color.HSBtoRGB(hueFromSetting(setting), sat, bri);
        Color c = new Color(rgb);
        setting.setValue(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    private void updateHue(ColorSetting setting, double mouseX) {
        float[] hsb = Color.RGBtoHSB(setting.getRed(), setting.getGreen(), setting.getBlue(), null);
        float hue = (float)((mouseX - lastHueX) / (float)SV_SIZE);
        hue = Math.max(0f, Math.min(1f, hue));

        int rgb = Color.HSBtoRGB(hue, hsb[1], hsb[2]);
        Color c = new Color(rgb);
        setting.setValue(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    private float hueFromSetting(ColorSetting setting) {
        float[] hsb = Color.RGBtoHSB(setting.getRed(), setting.getGreen(), setting.getBlue(), null);
        return hsb[0];
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

    private static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    @Override
    public boolean mouseReleased(ColorSetting setting, double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        boolean wasDragging = draggingHue || draggingSV;
        draggingHue = false;
        draggingSV = false;
        return wasDragging;
    }
}
