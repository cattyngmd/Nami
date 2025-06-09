package me.kiriyaga.essentials.feature.gui;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.module.impl.client.ClickGuiModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

public class ClickGuiScreen extends Screen {

    private final Set<Category> expandedCategories = new HashSet<>();
    private final Set<Module> expandedModules = new HashSet<>();
    private ClickGuiModule getClickGuiModule() {
        return MODULE_MANAGER.getModule(ClickGuiModule.class);
    }

    public ClickGuiScreen() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (getClickGuiModule() != null && getClickGuiModule().background.get())
            context.fill(0, 0, this.width, this.height, 0xC0101010);

        super.render(context, mouseX, mouseY, delta);


        int startX = 20;
        int startY = 20;

        int curX = startX;
        for (Category category : Category.values()) {
            CategoryPanel panel = new CategoryPanel(category, expandedCategories, expandedModules);
            panel.render(context, this.textRenderer, curX, startY, mouseX, mouseY);
            curX += CategoryPanel.WIDTH + CategoryPanel.GAP;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = 20;
        int startY = 20;

        int curX = startX;
        for (Category category : Category.values()) {
            if (CategoryPanel.isHovered(mouseX, mouseY, curX, startY)) {
                if (button == 0 || button == 1) {
                    if (expandedCategories.contains(category)) {
                        expandedCategories.remove(category);
                    } else {
                        expandedCategories.add(category);
                    }
                }
                return true;
            }

            if (expandedCategories.contains(category)) {
                List<Module> modules = MODULE_MANAGER.getModulesByCategory(category);
                int curY = startY + CategoryPanel.HEIGHT + CategoryPanel.GAP;

                for (int i = 0; i < modules.size(); i++) {
                    Module module = modules.get(i);

                    if (i != 0) {
                        curY += ModulePanel.PADDING;
                    }

                    if (ModulePanel.isHovered(mouseX, mouseY, curX, curY)) {
                        if (button == 0) {
                            module.toggle();
                        } else if (button == 1 || button == 0) {
                            if (expandedModules.contains(module)) {
                                expandedModules.remove(module);
                            } else {
                                expandedModules.add(module);
                            }
                        }
                        return true;
                    }
                    curY += ModulePanel.HEIGHT;

                    if (expandedModules.contains(module)) {
                        if (SettingPanel.mouseClicked(module, mouseX, mouseY, button, curX, curY)) {
                            return true;
                        }
                        curY += SettingPanel.getSettingsHeight(module);
                    }
                }
            }
            curX += CategoryPanel.WIDTH + CategoryPanel.GAP;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        SettingPanel.mouseDragged(mouseX);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        SettingPanel.mouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }



    @Override
    public boolean shouldPause() {
        return false;
    }

    private int getColorOrFallback(java.awt.Color awtColor, int fallback) {
        if (awtColor == null) return fallback;
        return ((awtColor.getAlpha() & 0xFF) << 24) |
                ((awtColor.getRed() & 0xFF) << 16) |
                ((awtColor.getGreen() & 0xFF) << 8) |
                (awtColor.getBlue() & 0xFF);
    }

}
