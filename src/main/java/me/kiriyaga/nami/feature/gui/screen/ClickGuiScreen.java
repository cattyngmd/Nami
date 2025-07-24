package me.kiriyaga.nami.feature.gui.screen;

import me.kiriyaga.nami.feature.gui.components.CategoryPanel;
import me.kiriyaga.nami.feature.gui.components.ModulePanel;
import me.kiriyaga.nami.feature.gui.components.SettingPanel;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.*;
import java.awt.Point;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class ClickGuiScreen extends Screen {
    private final Set<ModuleCategory> expandedCategories = new HashSet<>();
    private final Set<Module> expandedModules = new HashSet<>();
    private final Map<ModuleCategory, Point> categoryPositions = new HashMap<>();
    private int scrollOffset = 0;
    private boolean draggingCategory = false;
    private ModuleCategory draggedModuleCategory = null;
    public float scale = 1;

    private ClickGuiModule getClickGuiModule() {
        return MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
    }

    public ClickGuiScreen() {
        super(Text.literal("NamiGui"));
        syncCategoryPositions();
        expandedCategories.addAll(ModuleCategory.getAll());
    }

    private void syncCategoryPositions() {
        int x = 20;
        int y = 20;
        for (ModuleCategory moduleCategory : ModuleCategory.getAll()) {
            categoryPositions.putIfAbsent(moduleCategory, new Point(x, y));
            x += CategoryPanel.WIDTH + CategoryPanel.GAP;
        }
        categoryPositions.keySet().removeIf(cat -> !ModuleCategory.getAll().contains(cat));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        syncCategoryPositions();

        ClickGuiModule clickGuiModule = getClickGuiModule();
        if (clickGuiModule != null && clickGuiModule.background.get())
            context.fill(0, 0, this.width, this.height, 0xC0101010);

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale, scale);

        int scaledMouseX = (int) (mouseX / scale);
        int scaledMouseY = (int) (mouseY / scale);

        for (ModuleCategory moduleCategory : ModuleCategory.getAll()) {
            Point pos = categoryPositions.get(moduleCategory);
            if (pos == null) continue;

            CategoryPanel panel = new CategoryPanel(moduleCategory, expandedCategories, expandedModules);
            panel.render(context, this.textRenderer, pos.x, pos.y + scrollOffset, scaledMouseX, scaledMouseY, this.height);
        }

        if (clickGuiModule != null && clickGuiModule.descriptions.get()) {
            for (ModuleCategory moduleCategory : ModuleCategory.getAll()) {
                if (!expandedCategories.contains(moduleCategory)) continue;

                Point pos = categoryPositions.get(moduleCategory);
                if (pos == null) continue;

                List<Module> modules = MODULE_MANAGER.getStorage().getByCategory(moduleCategory);
                int curY = pos.y + CategoryPanel.HEADER_HEIGHT + scrollOffset + ModulePanel.MODULE_SPACING + CategoryPanel.BOTTOM_MARGIN;

                for (Module module : modules) {
                    int modX = pos.x + CategoryPanel.BORDER_WIDTH + SettingPanel.INNER_PADDING;
                    int modY = curY;

                    if (ModulePanel.isHovered(scaledMouseX, scaledMouseY, modX, modY)) {
                        String description = module.getDescription();
                        if (description != null && !description.isEmpty()) {
                            int descX = scaledMouseX + 5;
                            int descY = scaledMouseY;
                            int textWidth = textRenderer.getWidth(description);
                            int textHeight = 8;

                            context.fill(descX - 2, descY - 2, descX + textWidth + 2, descY + textHeight + 2,
                                    0x7F000000);
                            context.drawText(textRenderer, description, descX, descY, 0xFFFFFFFF, false);
                        }
                        context.getMatrices().popMatrix();
                        return;
                    }

                    curY += ModulePanel.HEIGHT + ModulePanel.MODULE_SPACING;

                    if (expandedModules.contains(module)) {
                        curY += SettingPanel.getSettingsHeight(module);
                    }
                }
            }
        }

        context.getMatrices().popMatrix();
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        syncCategoryPositions();

        int scaledMouseX = (int) (mouseX / scale);
        int scaledMouseY = (int) (mouseY / scale);

        for (ModuleCategory moduleCategory : ModuleCategory.getAll()) {
            Point pos = categoryPositions.get(moduleCategory);
            if (pos == null) continue;

            if (CategoryPanel.isHeaderHovered(scaledMouseX, scaledMouseY, pos.x, pos.y + scrollOffset)) {
                if (button == 0) {
                    draggingCategory = true;
                    draggedModuleCategory = moduleCategory;
                    return true;
                } else if (button == 1) {
                    if (expandedCategories.contains(moduleCategory)) {
                        expandedCategories.remove(moduleCategory);
                    } else {
                        expandedCategories.add(moduleCategory);
                    }
                    return true;
                }
            }
        }

        if (!draggingCategory) {
            for (ModuleCategory moduleCategory : ModuleCategory.getAll()) {
                if (expandedCategories.contains(moduleCategory)) {
                    Point pos = categoryPositions.get(moduleCategory);
                    if (pos == null) continue;

                    List<Module> modules = MODULE_MANAGER.getStorage().getByCategory(moduleCategory);
                    int curY = pos.y + CategoryPanel.HEADER_HEIGHT + scrollOffset + ModulePanel.MODULE_SPACING + CategoryPanel.BOTTOM_MARGIN;

                    for (Module module : modules) {
                        int modX = pos.x + CategoryPanel.BORDER_WIDTH + SettingPanel.INNER_PADDING;

                        if (ModulePanel.isHovered(scaledMouseX, scaledMouseY, modX, curY)) {
                            if (button == 0) {
                                module.toggle();
                            } else if (button == 1) {
                                if (expandedModules.contains(module)) {
                                    expandedModules.remove(module);
                                } else {
                                    expandedModules.add(module);
                                }
                            }
                            return true;
                        }

                        curY += ModulePanel.HEIGHT + ModulePanel.MODULE_SPACING;

                        if (expandedModules.contains(module)) {
                            if (SettingPanel.mouseClicked(module, scaledMouseX, scaledMouseY, button, modX, curY)) {
                                return true;
                            }
                            curY += SettingPanel.getSettingsHeight(module);
                        }
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (SettingPanel.keyPressed(keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int scaledMouseX = (int) (mouseX / scale);
        int scaledMouseY = (int) (mouseY / scale);
        int scaledDeltaX = (int) (deltaX / scale);
        int scaledDeltaY = (int) (deltaY / scale);

        if (draggingCategory && draggedModuleCategory != null) {
            Point currentPos = categoryPositions.get(draggedModuleCategory);
            if (currentPos != null) {
                currentPos.translate(scaledDeltaX, scaledDeltaY);
                return true;
            }
        }

        SettingPanel.mouseDragged(scaledMouseX, scaledMouseY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingCategory = false;
        draggedModuleCategory = null;
        SettingPanel.mouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset += verticalAmount * 20;
        scrollOffset = Math.min(scrollOffset, 0);
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
