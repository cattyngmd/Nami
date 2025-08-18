package me.kiriyaga.nami.feature.gui.components;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import java.awt.*;
import java.util.List;
import java.util.Set;

import static me.kiriyaga.nami.Nami.CLICK_GUI;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.*;
import static me.kiriyaga.nami.feature.gui.components.ModulePanel.MODULE_SPACING;

public class CategoryPanel {
    public static final int WIDTH = 110;
    public static final int HEADER_HEIGHT = 14;
    public static final int GAP = 5;
    private static final int PADDING = 5;
    public static final int BORDER_WIDTH = 1;
    public static final int BOTTOM_MARGIN = 1;

    private final ModuleCategory moduleCategory;
    private final Set<ModuleCategory> expandedCategories;
    private final Set<Module> expandedModules;

    private ColorModule getColorModule() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
    }

    private ClickGuiModule getClickGuiModule() {
        return MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
    }

    public CategoryPanel(ModuleCategory moduleCategory, Set<ModuleCategory> expandedCategories, Set<Module> expandedModules) {
        this.moduleCategory = moduleCategory;
        this.expandedCategories = expandedCategories;
        this.expandedModules = expandedModules;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY, int screenHeight) {
        boolean hovered = isHeaderHovered(mouseX, mouseY, x, y);
        boolean expanded = expandedCategories.contains(moduleCategory);

        ColorModule colorModule = getColorModule();
        Color primary = colorModule.getStyledGlobalColor();
        Color secondary = colorModule.getStyledSecondColor();
        Color textCol = MODULE_MANAGER.getStorage()
                .getByClass(ClickGuiModule.class)
                .moduleFill.get()
                ? new Color(255, 255, 255, 255)
                : new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);

        int totalHeight = HEADER_HEIGHT + BOTTOM_MARGIN + MODULE_SPACING;
        List<Module> modules = MODULE_MANAGER.getStorage().getByCategory(moduleCategory);
        if (expanded) {
            for (Module module : modules) {
                totalHeight += ModulePanel.HEIGHT + MODULE_SPACING;
                if (expandedModules.contains(module)) {
                    totalHeight += SettingPanel.getSettingsHeight(module);
                }
            }
            totalHeight += BOTTOM_MARGIN;
        }

        int bgColor = CLICK_GUI.applyFade(
                toRGBA(new Color(30, 30, 30, getClickGuiModule().guiAlpha.get()))
        );
        context.fill(x, y, x + WIDTH, y + totalHeight, bgColor);

        if (getClickGuiModule() != null && getClickGuiModule().lines.get()) {
            int lineColor = CLICK_GUI.applyFade(primary.getRGB());
            context.fill(x, y + HEADER_HEIGHT, x + WIDTH, y + HEADER_HEIGHT + 1, lineColor);
            context.fill(x, y + totalHeight - 1, x + WIDTH, y + totalHeight, lineColor);
            context.fill(x, y + HEADER_HEIGHT + 1, x + 1, y + totalHeight - 1, lineColor);
            context.fill(x + WIDTH - 1, y + HEADER_HEIGHT + 1, x + WIDTH, y + totalHeight - 1, lineColor);
        } else if (!getClickGuiModule().lines.get()){
            int lineColor = CLICK_GUI.applyFade(new Color(20, 20, 20, 122).getRGB());
            context.fill(x, y + HEADER_HEIGHT, x + WIDTH, y + HEADER_HEIGHT + 1, lineColor);
            context.fill(x, y + totalHeight - 1, x + WIDTH, y + totalHeight, lineColor);
            context.fill(x, y + HEADER_HEIGHT + 1, x + 1, y + totalHeight - 1, lineColor);
            context.fill(x + WIDTH - 1, y + HEADER_HEIGHT + 1, x + WIDTH, y + totalHeight - 1, lineColor);
        }

        context.fill(x + 1, y + HEADER_HEIGHT + 1, x + 2, y + totalHeight - 1, CLICK_GUI.applyFade(new Color(20, 20, 20, 122).getRGB()));
        context.fill(x + WIDTH - 2, y + HEADER_HEIGHT + 1, x + WIDTH - 1, y + totalHeight - 1, CLICK_GUI.applyFade(new Color(20, 20, 20, 122).getRGB()));

        context.fill(x, y, x + WIDTH, y + HEADER_HEIGHT, CLICK_GUI.applyFade(toRGBA(primary)));

        context.fill(
                x + 2,
                y + HEADER_HEIGHT + 1,
                x + WIDTH - 2,
                y + HEADER_HEIGHT + 2,
                CLICK_GUI.applyFade(new Color(20, 20, 20, 122).getRGB())
        );

        int textY = y + (HEADER_HEIGHT - textRenderer.fontHeight) / 2;
        context.drawText(textRenderer, moduleCategory.getName(),
                x + PADDING, textY,
                CLICK_GUI.applyFade(toRGBA(textCol)), false);

        if (expanded) {
            int moduleY = y + HEADER_HEIGHT + MODULE_SPACING + BOTTOM_MARGIN;
            for (int i = 0; i < modules.size(); i++) {
                Module module = modules.get(i);

                ModulePanel modulePanel = new ModulePanel(module, expandedModules);
                modulePanel.render(context, textRenderer,
                        x + BORDER_WIDTH + SettingPanel.INNER_PADDING,
                        moduleY, mouseX, mouseY);
                moduleY += ModulePanel.HEIGHT;

                if (expandedModules.contains(module)) {
                    moduleY += SettingPanel.renderSettings(context, textRenderer, module,
                            x + BORDER_WIDTH + SettingPanel.INNER_PADDING,
                            moduleY, mouseX, mouseY);
                }

                if (i < modules.size() - 1) {
                    moduleY += MODULE_SPACING;
                }
            }
        }
    }

    public static boolean isHeaderHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
    }
}