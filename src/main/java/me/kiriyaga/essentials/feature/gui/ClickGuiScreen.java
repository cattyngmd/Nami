package me.kiriyaga.essentials.feature.gui;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

public class ClickGuiScreen extends Screen {

    private static final int CATEGORY_WIDTH = 110;
    private static final int MODULE_WIDTH = 130;
    private static final int SETTING_WIDTH = 200;
    private static final int ITEM_HEIGHT = 20;
    private static final int PADDING = 6;
    private static final int GAP = 5;

    private Category selectedCategory = Category.COMBAT;
    private Module selectedModule = null;

    private final Set<Module> expandedModules = new HashSet<>();

    public ClickGuiScreen() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {


        context.fill(0, 0, this.width, this.height, 0xC0101010);

        int startX = 20;
        int startY = 20;

        int categoryX = startX;
        int categoryY = startY;

        for (Category category : Category.values()) {
            boolean selected = category == selectedCategory;

            int boxColor = selected ? 0xAAFFFF00 : 0xAA000000;
            context.fill(categoryX, categoryY, categoryX + CATEGORY_WIDTH, categoryY + ITEM_HEIGHT, boxColor);

            int textColor = selected ? 0xFFFFFF00 : 0xFFFFFFFF;
            context.drawText(this.textRenderer, category.name(), categoryX + PADDING, categoryY + 6, textColor, false);

            categoryX += CATEGORY_WIDTH + GAP;
        }

        int modulesStartY = categoryY + ITEM_HEIGHT + GAP;
        int modulesX = startX + (CATEGORY_WIDTH + GAP) * selectedCategory.ordinal();

        List<Module> modules = MODULE_MANAGER.getModulesByCategory(selectedCategory);
        int currentY = modulesStartY;

        for (Module module : modules) {
            boolean selected = module == selectedModule;

            int boxColor = selected ? 0xAA00FF00 : 0xAA000000;
            context.fill(modulesX, currentY, modulesX + MODULE_WIDTH, currentY + ITEM_HEIGHT, boxColor);

            int textColor = selected ? 0xFF00FF00 : 0xFFFFFFFF;
            context.drawText(this.textRenderer, module.getName(), modulesX + PADDING, currentY + 6, textColor, false);

            currentY += ITEM_HEIGHT;

            if (expandedModules.contains(module)) {
                List<Setting<?>> settings = module.getSettings();
                for (Setting<?> setting : settings) {
                    context.fill(modulesX + 10, currentY, modulesX + SETTING_WIDTH, currentY + ITEM_HEIGHT, 0xAA000000);
                    context.drawText(this.textRenderer, setting.getName() + ": " + setting.get(), modulesX + 10 + PADDING, currentY + 6, 0xFFFFFFFF, false);
                    currentY += ITEM_HEIGHT;
                }
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = 20;
        int startY = 20;

        int categoryX = startX;
        int categoryY = startY;

        for (Category category : Category.values()) {
            if (isHovered(mouseX, mouseY, categoryX, categoryY, CATEGORY_WIDTH, ITEM_HEIGHT)) {
                selectedCategory = category;
                selectedModule = null;
                expandedModules.clear();
                return true;
            }
            categoryX += CATEGORY_WIDTH + GAP;
        }

        int modulesX = startX + (CATEGORY_WIDTH + GAP) * selectedCategory.ordinal();
        int modulesStartY = categoryY + ITEM_HEIGHT + GAP;

        List<Module> modules = MODULE_MANAGER.getModulesByCategory(selectedCategory);
        int currentY = modulesStartY;

        for (Module module : modules) {
            if (isHovered(mouseX, mouseY, modulesX, currentY, MODULE_WIDTH, ITEM_HEIGHT)) {
                if (button == 0) {
                    module.toggle();
                    selectedModule = module;
                    if (!expandedModules.contains(module)) {
                        expandedModules.remove(module);
                    }
                } else if (button == 1) {
                    if (expandedModules.contains(module)) {
                        expandedModules.remove(module);
                    } else {
                        expandedModules.add(module);
                        selectedModule = module;
                    }
                }
                return true;
            }
            currentY += ITEM_HEIGHT;

            if (expandedModules.contains(module)) {
                currentY += ITEM_HEIGHT * module.getSettings().size();
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isHovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
