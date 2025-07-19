package me.kiriyaga.nami.feature.gui;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.*;
import java.util.List;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class ClickGuiScreen extends Screen {
    private final Set<ModuleCategory> expandedCategories = new HashSet<>();
    private final Set<Module> expandedModules = new HashSet<>();
    private final Map<ModuleCategory, Integer> categoryPositions = new HashMap<>();
    private int scrollOffset = 0;
    private boolean draggingCategory = false;
    private ModuleCategory draggedModuleCategory = null;
    private int dragStartX = 0;
    public static final int GUI_ALPHA = 122;

    private ClickGuiModule getClickGuiModule() {
        return MODULE_MANAGER.getModule(ClickGuiModule.class);
    }

    public ClickGuiScreen() {
        super(Text.literal("ClickGUI"));
        syncCategoryPositions();
    }

    private void syncCategoryPositions() {
        int x = 20;
        for (ModuleCategory moduleCategory : ModuleCategory.getAll()) {
            categoryPositions.putIfAbsent(moduleCategory, x);
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

        super.render(context, mouseX, mouseY, delta);

        for (ModuleCategory moduleCategory : ModuleCategory.getAll()) {
            try {
                Integer x = categoryPositions.get(moduleCategory);
                if (x == null) continue; // пропускаем, если позиции нет

                CategoryPanel panel = new CategoryPanel(moduleCategory, expandedCategories, expandedModules);
                panel.render(context, this.textRenderer, x, 20 + scrollOffset, mouseX, mouseY, this.height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (clickGuiModule != null && clickGuiModule.descriptions.get()) {
            for (ModuleCategory moduleCategory : ModuleCategory.getAll()) {
                if (!expandedCategories.contains(moduleCategory)) continue;

                Integer x = categoryPositions.get(moduleCategory);
                if (x == null) continue;

                List<Module> modules = MODULE_MANAGER.getModulesByCategory(moduleCategory);
                int curY = 20 + CategoryPanel.HEADER_HEIGHT + scrollOffset + ModulePanel.MODULE_SPACING;

                for (Module module : modules) {
                    int modX = x + CategoryPanel.BORDER_WIDTH + SettingPanel.INNER_PADDING;
                    int modY = curY;

                    if (ModulePanel.isHovered(mouseX, mouseY, modX, modY)) {
                        String description = module.getDescription();
                        if (description != null && !description.isEmpty()) {

                            int descX = mouseX + 5;
                            int descY = mouseY;
                            int textWidth = textRenderer.getWidth(description);
                            int textHeight = 8;

                            context.fill(descX - 2, descY - 2, descX + textWidth + 2, descY + textHeight + 2,
                                    new Color(30, 30, 30, GUI_ALPHA).getRGB());

                            context.drawText(textRenderer, description, descX, descY, 0xFFFFFFFF, false);
                        }
                        return;
                    }

                    curY += ModulePanel.HEIGHT + ModulePanel.MODULE_SPACING;

                    if (expandedModules.contains(module)) {
                        curY += SettingPanel.getSettingsHeight(module);
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        syncCategoryPositions();

        for (ModuleCategory moduleCategory : ModuleCategory.getAll()) {
            Integer x = categoryPositions.get(moduleCategory);
            if (x == null) continue;

            if (CategoryPanel.isHeaderHovered(mouseX, mouseY, x, 20 + scrollOffset)) {
                if (button == 0) {
                    draggingCategory = true;
                    draggedModuleCategory = moduleCategory;
                    dragStartX = (int) mouseX;
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
                    Integer x = categoryPositions.get(moduleCategory);
                    if (x == null) continue;

                    List<Module> modules = MODULE_MANAGER.getModulesByCategory(moduleCategory);
                    int curY = 20 + CategoryPanel.HEADER_HEIGHT + scrollOffset + ModulePanel.MODULE_SPACING;

                    for (Module module : modules) {
                        if (ModulePanel.isHovered(mouseX, mouseY, x + CategoryPanel.BORDER_WIDTH + SettingPanel.INNER_PADDING, curY)) {
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
                            if (SettingPanel.mouseClicked(module, mouseX, mouseY, button,
                                    x + CategoryPanel.BORDER_WIDTH + SettingPanel.INNER_PADDING,
                                    curY)) {
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
        if (draggingCategory && draggedModuleCategory != null) {
            Integer currentX = categoryPositions.get(draggedModuleCategory);
            if (currentX != null) {
                int newX = currentX + (int) deltaX;
                categoryPositions.put(draggedModuleCategory, newX);
                return true;
            }
        }

        SettingPanel.mouseDragged(mouseX);
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
        scrollOffset += verticalAmount * 10;
        scrollOffset = Math.min(scrollOffset, 0);
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
