package me.kiriyaga.essentials.feature.gui;

import me.kiriyaga.essentials.Essentials;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.module.impl.client.ColorModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.Set;

public class ModulePanel {
    public static final int WIDTH = 110;
    public static final int HEIGHT = 20;
    public static final int PADDING = 5;

    private final Module module;
    private final Set<Module> expandedModules;

    private final ColorModule colorModule = (ColorModule) Essentials.MODULE_MANAGER.getModule(ColorModule.class);

    public ModulePanel(Module module, Set<Module> expandedModules) {
        this.module = module;
        this.expandedModules = expandedModules;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);
        boolean expanded = expandedModules.contains(module);
        boolean enabled = module.isEnabled();

        Color primary = colorModule.primaryColor.get();
        Color secondary = colorModule.secondaryColor.get();
        Color textCol = colorModule.textColor.get();

        Color bgColor;


        if (hovered) {
            bgColor = brighten(secondary, 0.4f);
        } else if (enabled) {
            bgColor = primary;
        } else {
            bgColor = secondary;
        }

        int bgColorInt = toRGBA(bgColor);
        int textColorInt = toRGBA(textCol);

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);
        context.drawText(textRenderer, module.getName(), x + PADDING, y + 6, textColorInt, false);
    }

    private int toRGBA(Color color) {
        return (color.getAlpha() << 24) |
                (color.getRed() << 16) |
                (color.getGreen() << 8) |
                color.getBlue();
    }

    private Color brighten(Color color, float amount) {
        int r = Math.min(255, (int)(color.getRed() + 255 * amount));
        int g = Math.min(255, (int)(color.getGreen() + 255 * amount));
        int b = Math.min(255, (int)(color.getBlue() + 255 * amount));
        return new Color(r, g, b, color.getAlpha());
    }

    public static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }
}
