package me.kiriyaga.essentials.feature.gui;

import me.kiriyaga.essentials.Essentials;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.module.impl.client.ColorModule;
import me.kiriyaga.essentials.setting.Setting;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.setting.impl.EnumSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.List;

import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

public class SettingPanel {
    public static final int HEIGHT = 20;
    private static final int PADDING = 6;
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

        if (colorModule == null)
            return;

        boolean hovered = isHovered(mouseX, mouseY, x, y);
        Color primary = colorModule.primaryColor.get();
        Color secondary = colorModule.secondaryColor.get();
        Color textCol = colorModule.textColor.get();

        Color bgColor;

        if (setting instanceof BoolSetting boolSetting) {
            if (boolSetting.get()) {
                bgColor = primary;
            } else {
                bgColor = secondary;
            }
            if (hovered) {
                bgColor = brighten(bgColor, 0.3f);
            }
        } else {
            bgColor = hovered ? brighten(secondary, 0.3f) : secondary;
        }

        int bgColorInt = toRGBA(bgColor);
        int textColorInt = toRGBA(textCol);

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);

        String text = setting.getName() + ": " + setting.get();
        context.drawText(textRenderer, text, x + PADDING, y + 6, textColorInt, false);
    }

    public static boolean mouseClicked(Module module, double mouseX, double mouseY, int button, int x, int y) {
        if (button != 0 && button != 1) return false; // реагируем на ЛКМ(0) и ПКМ(1)

        List<Setting<?>> settings = module.getSettings();
        int curY = y;

        for (Setting<?> setting : settings) {
            if (isHovered(mouseX, mouseY, x, curY)) {
                if (setting instanceof BoolSetting boolSetting) {
                    boolSetting.toggle();
                    return true;
                }
                else if (setting instanceof IntSetting intSetting) {
                    int step = 1;
                    int current = intSetting.get();
                    int min = intSetting.getMin();
                    int max = intSetting.getMax();

                    if (button == 0 && current < max) {
                        intSetting.set(current + step);
                    } else if (button == 1 && current > min) {
                        intSetting.set(current - step);
                    }
                    return true;
                }
                else if (setting instanceof DoubleSetting doubleSetting) {
                    double step = 0.1;
                    double current = doubleSetting.get();
                    double min = doubleSetting.getMin();
                    double max = doubleSetting.getMax();

                    if (button == 0 && current < max) {
                        doubleSetting.set(Math.min(current + step, max));
                    } else if (button == 1 && current > min) {
                        doubleSetting.set(Math.max(current - step, min));
                    }
                    return true;
                }
                else if (setting instanceof EnumSetting<?> enumSetting) {
                    enumSetting.cycle();
                    return true;
                }
            }
            curY += HEIGHT;
        }
        return false;
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
}
