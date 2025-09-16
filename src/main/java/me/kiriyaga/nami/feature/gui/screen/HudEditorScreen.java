package me.kiriyaga.nami.feature.gui.screen;

import me.kiriyaga.nami.feature.gui.components.CategoryPanel;
import me.kiriyaga.nami.feature.gui.components.ModulePanel;
import me.kiriyaga.nami.feature.gui.components.SettingPanel;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.HudEditorModule;
import me.kiriyaga.nami.util.ChatAnimationHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Point;
import java.util.*;

import static me.kiriyaga.nami.Nami.*;

public class HudEditorScreen extends Screen {
    private final Set<ModuleCategory> expandedCategories = new HashSet<>();
    private final Set<Module> expandedModules = new HashSet<>();
    private final Map<ModuleCategory, Point> categoryPositions = new HashMap<>();
    private int scrollOffset = 0;
    private boolean draggingCategory = false;
    private ModuleCategory draggedModuleCategory = null;
    public float scale = 1;

    private HudElementModule draggingElement = null;
    private int dragOffsetX, dragOffsetY;

    public HudEditorScreen() {
        super(Text.literal("NamiHudEditor"));
        syncCategoryPositions();
        expandedCategories.add(ModuleCategory.of("hud"));
    }

    private HudEditorModule getHudEditorModule() {
        return MODULE_MANAGER.getStorage().getByClass(HudEditorModule.class);
    }

    private void syncCategoryPositions() {
        int x = 20;
        int y = 20;
        ModuleCategory hud = ModuleCategory.of("hud");
        categoryPositions.putIfAbsent(hud, new Point(x, y));
        categoryPositions.keySet().removeIf(cat -> !cat.equals(hud));
    }

    @Override
    public void renderBackground(DrawContext context, int i, int j, float f) {}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        syncCategoryPositions();

        HudEditorModule hudEditorModule = getHudEditorModule();
        if (hudEditorModule != null && hudEditorModule.background.get()) {
            int alpha = (hudEditorModule.backgroundAlpha.get() & 0xFF) << 24;
            int color = alpha | 0x101010;
            context.fill(0, 0, this.width, this.height, color);
        }

        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1.0f);

        int scaledMouseX = (int) (mouseX / scale);
        int scaledMouseY = (int) (mouseY / scale);

        ModuleCategory hudCategory = ModuleCategory.of("hud");
        Point pos = categoryPositions.get(hudCategory);
        if (pos != null) {
            CategoryPanel panel = new CategoryPanel(hudCategory, expandedCategories, expandedModules);
            panel.render(context, this.textRenderer, pos.x, pos.y + scrollOffset, scaledMouseX, scaledMouseY, this.height);
        }

        ClickGuiModule clickGuiModule = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
        if (clickGuiModule != null && clickGuiModule.descriptions.get() && pos != null && expandedCategories.contains(hudCategory)) {
            List<Module> modules = MODULE_MANAGER.getStorage().getByCategory(hudCategory);
            int curY = pos.y + CategoryPanel.HEADER_HEIGHT + scrollOffset + ModulePanel.MODULE_SPACING + CategoryPanel.BOTTOM_MARGIN;

            for (Module module : modules) {
                int modX = pos.x + CategoryPanel.BORDER_WIDTH + SettingPanel.INNER_PADDING;

                if (ModulePanel.isHovered(scaledMouseX, scaledMouseY, modX, curY)) {
                    String description = module.getDescription();
                    if (description != null && !description.isEmpty()) {
                        int descX = scaledMouseX + 5;
                        int descY = scaledMouseY;
                        int textWidth = FONT_MANAGER.getWidth(Text.of(description));
                        int textHeight = 8;

                        context.fill(descX - 2, descY - 2, descX + textWidth + 2, descY + textHeight + 2, 0x7F000000);
                        //context.drawText(textRenderer, description, descX, descY, 0xFFFFFFFF, true);
                        FONT_MANAGER.drawText(context, Text.of(description), descX, descY, true);
                    }
                    context.getMatrices().pop();
                    super.render(context, mouseX, mouseY, delta);
                    return;
                }

                curY += ModulePanel.HEIGHT + ModulePanel.MODULE_SPACING;
                if (expandedModules.contains(module)) {
                    curY += SettingPanel.getSettingsHeight(module);
                }
            }
        }

        context.getMatrices().pop();

        int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();
        int screenHeight = MC.getWindow().getScaledHeight();
        int chatZoneTop = screenHeight - (screenHeight / 8);

        for (Module module : MODULE_MANAGER.getStorage().getByCategory(ModuleCategory.of("hud"))) {
            if (module instanceof HudElementModule hud && hud.isEnabled()) {
                int y = hud.getRenderY();

                boolean isInChatZone = (y + hud.height) >= chatZoneTop;
                int renderY = isInChatZone ? y - chatAnimationOffset : y;

                int baseX = hud.getRenderX();

                boolean hovered = mouseX >= baseX && mouseX <= baseX + hud.width &&
                        mouseY >= renderY && mouseY <= renderY + hud.height;

                if (hovered) {
                    context.fill(
                            baseX - 1, renderY - 1,
                            baseX + hud.width + 1, renderY + hud.height + 1,
                            0x50FFFFFF
                    );
                }

                for (HudElementModule.TextElement element : new ArrayList<>(hud.getTextElements())) {
                    int drawX = hud.getRenderXForElement(element);
                    int drawY = renderY + element.offsetY();
                    //context.drawText(MC.textRenderer, element.text(), drawX, drawY, 0xFFFFFFFF, true);
                    FONT_MANAGER.drawText(context, element.text(), drawX, drawY, true);
                }
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        syncCategoryPositions();

        int scaledMouseX = (int) (mouseX / scale);
        int scaledMouseY = (int) (mouseY / scale);

        ModuleCategory hudCategory = ModuleCategory.of("hud");
        Point pos = categoryPositions.get(hudCategory);

        if (pos != null && CategoryPanel.isHeaderHovered(scaledMouseX, scaledMouseY, pos.x, pos.y + scrollOffset)) {
            if (button == 0) {
                playClickSound();
                draggingCategory = true;
                draggedModuleCategory = hudCategory;
                return true;
            } else if (button == 1) {
                if (expandedCategories.contains(hudCategory)) {
                    expandedCategories.remove(hudCategory);
                } else {
                    expandedCategories.add(hudCategory);
                }
                playClickSound();
                return true;
            }
        }

        if (!draggingCategory && pos != null && expandedCategories.contains(hudCategory)) {
            List<Module> modules = MODULE_MANAGER.getStorage().getByCategory(hudCategory);
            int curY = pos.y + CategoryPanel.HEADER_HEIGHT + scrollOffset + ModulePanel.MODULE_SPACING + CategoryPanel.BOTTOM_MARGIN;

            for (Module module : modules) {
                int modX = pos.x + CategoryPanel.BORDER_WIDTH + SettingPanel.INNER_PADDING;

                if (ModulePanel.isHovered(scaledMouseX, scaledMouseY, modX, curY)) {
                    if (button == 0) {
                        playClickSound();
                        module.toggle();
                    } else if (button == 1) {
                        if (expandedModules.contains(module)) {
                            expandedModules.remove(module);
                        } else {
                            expandedModules.add(module);
                        }
                        playClickSound();
                    } else if (button == 2) {
                        playClickSound();
                        module.setDrawn(!module.isDrawn());
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

        if (button != 0) return false;

        int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();
        int screenHeight = MC.getWindow().getScaledHeight();
        int chatZoneTop = screenHeight - (screenHeight / 8);

        for (Module module : MODULE_MANAGER.getStorage().getByCategory(ModuleCategory.of("hud"))) {
            if (module instanceof HudElementModule hud && hud.isEnabled()) {
                int x = hud.getRenderX();
                int y = hud.getRenderY();
                int renderY = (y + hud.height) >= chatZoneTop ? y - chatAnimationOffset : y;

                if (mouseX >= x && mouseX <= x + hud.width &&
                        mouseY >= renderY && mouseY <= renderY + hud.height) {
                    draggingElement = hud;
                    dragOffsetX = (int) mouseX - x;
                    dragOffsetY = (int) mouseY - renderY;
                    return true;
                }
            }
        }

        return false;
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

        if (button == 0 && draggingElement != null) {
            int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();

            int newRenderX = (int) mouseX - dragOffsetX;
            int newRenderY = (int) mouseY - dragOffsetY + chatAnimationOffset;

            int screenWidth = MC.getWindow().getScaledWidth();
            int screenHeight = MC.getWindow().getScaledHeight();

            newRenderY = Math.max(1, Math.min(newRenderY, screenHeight - draggingElement.height - 1));

            int newX;
            switch (draggingElement.alignment.get()) {
                case left:
                    newRenderX = Math.max(1, Math.min(newRenderX, screenWidth - draggingElement.width - 1));
                    newX = newRenderX;
                    break;
                case center:
                    newRenderX = Math.max(draggingElement.width / 2, Math.min(newRenderX, screenWidth - draggingElement.width / 2));
                    newX = newRenderX + draggingElement.width / 2;
                    break;
                case right:
                    newRenderX = Math.max(0, Math.min(newRenderX, screenWidth - draggingElement.width));
                    newX = newRenderX + draggingElement.width;
                    break;
                default:
                    newRenderX = Math.max(1, Math.min(newRenderX, screenWidth - draggingElement.width - 1));
                    newX = newRenderX;
                    break;
            }

            boolean intersects = false;
            for (Module module : MODULE_MANAGER.getStorage().getByCategory(ModuleCategory.of("hud"))) {
                if (module instanceof HudElementModule other && other.isEnabled() && other != draggingElement) {
                    int ox = other.getRenderX();
                    int oy = other.getRenderY();
                    int oWidth = other.width;
                    int oHeight = other.height;

                    boolean overlapX = newRenderX < ox + oWidth && newRenderX + draggingElement.width > ox;
                    boolean overlapY = newRenderY < oy + oHeight && newRenderY + draggingElement.height > oy;

                    if (overlapX && overlapY) {
                        intersects = true;
                        break;
                    }
                }
            }

            if (!intersects) {
                draggingElement.x.set(newX / (double) screenWidth);
                draggingElement.y.set(newRenderY / (double) screenHeight);
            }
            return true;
        }

        SettingPanel.mouseDragged(scaledMouseX, scaledMouseY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingCategory = false;
        draggedModuleCategory = null;

        if (button == 0) draggingElement = null;

        SettingPanel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset += verticalAmount * 20;
        scrollOffset = Math.min(scrollOffset, 0);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == MODULE_MANAGER.getStorage().getByClass(HudEditorModule.class).getKeyBind().get() && MC.world != null) {
            MC.setScreen(null);
            return true;
        }
        if (SettingPanel.keyPressed(keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void playClickSound() {
        MC.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(
                net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0f
        ));
    }
}