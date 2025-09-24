package me.kiriyaga.nami.feature.gui.components;

import me.kiriyaga.nami.feature.gui.base.PanelRenderer;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import java.awt.*;
import java.util.List;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.*;
import static me.kiriyaga.nami.feature.gui.components.ModulePanel.MODULE_SPACING;

public class CategoryPanel {
    public static final int WIDTH = 100;
    public static final int HEADER_HEIGHT = 12;
    private static final int PADDING = 5;
    public static final int BORDER_WIDTH = 1;
    public static final int BOTTOM_MARGIN = 1;

    private final ModuleCategory moduleCategory;
    private final Set<ModuleCategory> expandedCategories;
    private final Set<Module> expandedModules;
    private final PanelRenderer renderer = new PanelRenderer();

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
        boolean expanded = expandedCategories.contains(moduleCategory);


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

        renderer.renderPanel(context, x, y, WIDTH, totalHeight, HEADER_HEIGHT);
        renderer.renderHeaderText(context, textRenderer, moduleCategory.getName(), x, y, HEADER_HEIGHT, PADDING);

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