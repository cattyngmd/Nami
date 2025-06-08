package me.kiriyaga.essentials.feature.gui;

import me.kiriyaga.essentials.Essentials;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.module.impl.client.ColorModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

public class CategoryPanel {
    public static final int WIDTH = 110;
    public static final int HEIGHT = 20;
    public static final int GAP = 5;
    private static final int PADDING = 6;

    private static final ColorModule colorModule = (ColorModule) Essentials.MODULE_MANAGER.getModule(ColorModule.class);

    private final Category category;
    private final Set<Category> expandedCategories;
    private final Set<Module> expandedModules;

    public CategoryPanel(Category category, Set<Category> expandedCategories, Set<Module> expandedModules) {
        this.category = category;
        this.expandedCategories = expandedCategories;
        this.expandedModules = expandedModules;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);

        Color primary = colorModule.primaryColor.get();
        Color secondary = colorModule.secondaryColor.get();
        Color textCol = colorModule.textColor.get();

        Color bgColor;
        Color textColor;

        if (expandedCategories.contains(category)) {
            bgColor = primary;
            textColor = textCol;
        } else if (hovered) {
            bgColor = brighten(secondary, 0.3f);
            textColor = textCol;
        } else {
            bgColor = secondary;
            textColor = textCol;
        }

        context.fill(x, y, x + WIDTH, y + HEIGHT, toRGBA(bgColor));
        context.drawText(textRenderer, category.name(), x + PADDING, y + 6, toRGBA(textColor), false);

        if (expandedCategories.contains(category)) {
            int moduleY = y + HEIGHT + GAP;
            List<Module> modules = MODULE_MANAGER.getModulesByCategory(category);

            for (int i = 0; i < modules.size(); i++) {
                Module module = modules.get(i);

                if (i != 0) {
                    moduleY += ModulePanel.PADDING;
                }

                ModulePanel modulePanel = new ModulePanel(module, expandedModules);
                modulePanel.render(context, textRenderer, x, moduleY, mouseX, mouseY);
                moduleY += ModulePanel.HEIGHT;

                if (expandedModules.contains(module)) {
                    moduleY += SettingPanel.renderSettings(context, textRenderer, module, x, moduleY, mouseX, mouseY);
                }
            }
        }
    }


    public static boolean isHovered(double mouseX, double mouseY, int x, int y) {
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
