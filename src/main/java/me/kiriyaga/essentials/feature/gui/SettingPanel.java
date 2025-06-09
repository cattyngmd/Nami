package me.kiriyaga.essentials.feature.gui;

import me.kiriyaga.essentials.Essentials;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.module.impl.client.ColorModule;
import me.kiriyaga.essentials.setting.Setting;
import me.kiriyaga.essentials.setting.impl.*;
import me.kiriyaga.essentials.util.KeyUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.List;

import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

public class SettingPanel {
    public static final int HEIGHT = 20;
    private static final int PADDING = 5;
    private static final int WIDTH = 110;

    private static ColorModule getColorModule() {
        return MODULE_MANAGER.getModule(ColorModule.class);
    }

    public static int getSettingsHeight(Module module) {
        return module.getSettings().size() * HEIGHT;
    }

    public static int renderSettings(DrawContext context, TextRenderer textRenderer, Module module, int x, int y, int mouseX, int mouseY) {
        List<Setting<?>> settings = module.getSettings();
        int curY = y;
        for (Setting<?> setting : settings) {
            render(context, textRenderer, setting, x, curY, mouseX, mouseY);
            curY += HEIGHT;
        }
        return settings.size() * HEIGHT;
    }

    public static void render(DrawContext context, TextRenderer textRenderer, Setting<?> setting, int x, int y, int mouseX, int mouseY) {
        ColorModule colorModule = getColorModule();
        if (colorModule == null) return;

        boolean hovered = isHovered(mouseX, mouseY, x, y);
        Color primary = colorModule.getStyledPrimaryColor();
        Color secondary = colorModule.getStyledSecondaryColor();
        Color textCol = colorModule.getStyledTextColor();


        Color bgColor;

        if (setting instanceof BoolSetting boolSetting) {
            bgColor = boolSetting.get() ? primary : secondary;
            if (hovered) bgColor = brighten(bgColor, 0.3f);
        } else if (setting instanceof ColorSetting colorSetting) {
            float[] hsb = Color.RGBtoHSB(colorSetting.getRed(), colorSetting.getGreen(), colorSetting.getBlue(), null);
            float hue = hsb[0];

            bgColor = hovered ? brighten(secondary, 0.3f) : secondary;

            int bgColorInt = toRGBA(bgColor);
            int textColorInt = toRGBA(textCol);

            context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);

            String text = setting.getName();
            context.drawText(textRenderer, text, x + PADDING, y + 6, textColorInt, false);

            renderHueSlider(context, x + PADDING, y + HEIGHT - 6, WIDTH - 2 * PADDING, 4, hue);

            String hex = String.format("#%02X%02X%02X", colorSetting.getRed(), colorSetting.getGreen(), colorSetting.getBlue());
            context.drawText(textRenderer, hex, x + WIDTH - PADDING - textRenderer.getWidth(hex), y + 6, textColorInt, false);

            return;
        } else {
            bgColor = hovered ? brighten(secondary, 0.3f) : secondary;
        }

        int bgColorInt = toRGBA(bgColor);
        int textColorInt = toRGBA(textCol);

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);
        context.drawText(textRenderer, setting.getName(), x + PADDING, y + 6, textColorInt, false);

        if (setting instanceof IntSetting intSetting) {
            renderSlider(context, x + PADDING, y + HEIGHT - 6,
                    WIDTH - 2 * PADDING, 4,
                    intSetting.get(), intSetting.getMin(), intSetting.getMax(), primary);

            String valStr = String.valueOf(intSetting.get());
            context.drawText(textRenderer, valStr, x + WIDTH - PADDING - textRenderer.getWidth(valStr), y + 6, textColorInt, false);

            if (hovered) {
                context.drawText(textRenderer, setting.getName(), x + PADDING, y + 6, toRGBA(brighten(textCol, 0.5f)), false);
            }
        }
        else if (setting instanceof EnumSetting<?> enumSetting) {
            String valueStr = enumSetting.get().toString();
            context.drawText(textRenderer, valueStr, x + WIDTH - PADDING - textRenderer.getWidth(valueStr), y + 6, textColorInt, false);

            if (hovered) {
                context.drawText(textRenderer, setting.getName(), x + PADDING, y + 6, toRGBA(brighten(textCol, 0.5f)), false);
            }
        }
        else if (setting instanceof KeyBindSetting bindSetting) {
            String valueStr = KeyUtils.getKeyName(bindSetting.get());
            context.drawText(textRenderer, valueStr, x + WIDTH - PADDING - textRenderer.getWidth(valueStr), y + 6, textColorInt, false);

            if (hovered) {
                context.drawText(textRenderer, setting.getName(), x + PADDING, y + 6, toRGBA(brighten(textCol, 0.5f)), false);
            }
        }

        else if (setting instanceof DoubleSetting doubleSetting) {
            renderSlider(context, x + PADDING, y + HEIGHT - 6,
                    WIDTH - 2 * PADDING, 4,
                    doubleSetting.get(), doubleSetting.getMin(), doubleSetting.getMax(), primary);

            String valStr = String.format("%.1f", doubleSetting.get());
            context.drawText(textRenderer, valStr, x + WIDTH - PADDING - textRenderer.getWidth(valStr), y + 6, textColorInt, false);

            if (hovered) {
                context.drawText(textRenderer, setting.getName(), x + PADDING, y + 6, toRGBA(brighten(textCol, 0.5f)), false);
            }
        }
    }



    private static void renderHueSlider(DrawContext context, int x, int y, int width, int height, float hue) {
        for (int i = 0; i < width; i++) {
            float h = i / (float) width;
            Color color = Color.getHSBColor(h, 1f, 1f);
            context.fill(x + i, y, x + i + 1, y + height, toRGBA(color));
        }
        int pos = (int) (hue * width);
        context.fill(x + pos - 1, y, x + pos + 1, y + height, toRGBA(Color.WHITE));
    }


    private static void renderSlider(DrawContext context, int x, int y, int width, int height,
                                     double value, double min, double max, Color color) {

        context.fill(x, y, x + width, y + height, toRGBA(new Color(60, 60, 60, 150)));
        double percent = (value - min) / (max - min);
        int filledWidth = (int) (width * percent);
        context.fill(x, y, x + filledWidth, y + height, toRGBA(color));
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    private static int toRGBA(Color color) {
        return (color.getAlpha() << 24) |
                (color.getRed() << 16) |
                (color.getGreen() << 8) |
                color.getBlue();
    }

    private static Color brighten(Color color, float amount) {
        int r = Math.min(255, (int)(color.getRed() + 255 * amount));
        int g = Math.min(255, (int)(color.getGreen() + 255 * amount));
        int b = Math.min(255, (int)(color.getBlue() + 255 * amount));
        return new Color(r, g, b, color.getAlpha());
    }

    private static boolean dragging = false;
    private static Setting<?> draggedSetting = null;
    private static int dragStartX = 0;
    private static double startValue = 0;

    public static boolean mouseClicked(Module module, double mouseX, double mouseY, int button, int x, int y) {
        if (button != 0 && button != 1) return false;

        List<Setting<?>> settings = module.getSettings();
        int curY = y;

        for (Setting<?> setting : settings) {
            if (isHovered(mouseX, mouseY, x, curY)) {
                if (setting instanceof BoolSetting boolSetting) {
                    boolSetting.toggle();
                    return true;
                } else if (setting instanceof IntSetting intSetting) {
                    startDragging(setting, mouseX);
                    return true;
                } else if (setting instanceof DoubleSetting doubleSetting) {
                    startDragging(setting, mouseX);
                    return true;
                } else if (setting instanceof EnumSetting<?> enumSetting) {
                    enumSetting.cycle();
                    return true;
                } else if (setting instanceof ColorSetting colorSetting) {
                    startDragging(setting, mouseX);
                    return true;
                }
            }
            curY += HEIGHT;
        }
        return false;
    }

    public static void mouseDragged(double mouseX) {
        if (!dragging || draggedSetting == null) return;

        double deltaX = mouseX - dragStartX;

        if (draggedSetting instanceof ColorSetting colorSetting) {
            float[] hsb = Color.RGBtoHSB(colorSetting.getRed(), colorSetting.getGreen(), colorSetting.getBlue(), null);
            float newHue = (float) (startValue + deltaX * 0.005);
            newHue = Math.min(1f, Math.max(0f, newHue));
            Color newColor = Color.getHSBColor(newHue, 1f, 1f);
            colorSetting.setValue(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), colorSetting.getAlpha());
        } else if (draggedSetting instanceof IntSetting intSetting) {
            int newValue = slideInt((int) startValue, deltaX);
            intSetting.set(newValue);
        } else if (draggedSetting instanceof DoubleSetting doubleSetting) {
            double newValue = slideDouble(startValue, deltaX);
            doubleSetting.set(newValue);
        }
    }


    public static void mouseReleased() {
        dragging = false;
        draggedSetting = null;
    }

    private static void startDragging(Setting<?> setting, double mouseX) {
        dragging = true;
        draggedSetting = setting;
        dragStartX = (int) mouseX;
        if (setting instanceof IntSetting intSetting) {
            startValue = intSetting.get();
        } else if (setting instanceof DoubleSetting doubleSetting) {
            startValue = doubleSetting.get();
        } else if (setting instanceof ColorSetting colorSetting) {
            float[] hsb = Color.RGBtoHSB(colorSetting.getRed(), colorSetting.getGreen(), colorSetting.getBlue(), null);
            startValue = hsb[0];
        }
    }

    private static int slideInt(int start, double deltaX) {
        int step = 1;
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;

        if (draggedSetting instanceof IntSetting intSetting) {
            min = intSetting.getMin();
            max = intSetting.getMax();
        }

        double sensitivity = 0.2;
        int delta = (int) Math.round(deltaX * sensitivity);

        int newValue = start + delta * step;

        if (newValue < min) newValue = min;
        if (newValue > max) newValue = max;

        return newValue;
    }

    private static double slideDouble(double start, double deltaX) {
        double step = 0.1;
        double min = Double.NEGATIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;

        if (draggedSetting instanceof DoubleSetting doubleSetting) {
            min = doubleSetting.getMin();
            max = doubleSetting.getMax();
        }

        double sensitivity = 0.02;
        double delta = deltaX * sensitivity;

        double newValue = start + delta;

        newValue = Math.round(newValue / step) * step;

        if (newValue < min) newValue = min;
        if (newValue > max) newValue = max;

        return newValue;
    }
}
