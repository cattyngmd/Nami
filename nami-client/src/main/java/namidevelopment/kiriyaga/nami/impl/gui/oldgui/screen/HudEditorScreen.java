package namidevelopment.kiriyaga.nami.impl.gui.oldgui.screen;

import namidevelopment.kiriyaga.nami.impl.gui.oldgui.components.CategoryPanel;
import namidevelopment.kiriyaga.nami.impl.gui.oldgui.components.FeaturePanel;
import namidevelopment.kiriyaga.nami.impl.gui.oldgui.components.SettingPanel;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.HudElementFeature;
import namidevelopment.kiriyaga.nami.impl.feature.client.ClickGuiFeature;
import namidevelopment.kiriyaga.nami.impl.feature.client.ColorFeature;
import namidevelopment.kiriyaga.nami.util.ChatAnimationHelper;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.awt.Point;
import java.util.*;

import static namidevelopment.kiriyaga.nami.Nami.*;

public class HudEditorScreen extends Screen {

    private final Map<FeatureCategory, Point> categoryPositions = new HashMap<>();
    private final Map<FeatureCategory, CategoryPanel> categoryPanels = new HashMap<>();

    private boolean draggingCategory = false;
    private FeatureCategory draggedFeatureCategory = null;
    private int dragStartX, dragStartY;
    private int initialCategoryX, initialCategoryY;

    private HudElementFeature draggingElement = null;
    private int dragOffsetX, dragOffsetY;

    public HudEditorScreen() {
        super(Component.literal("NamiHudEditor"));
        initPanels();
    }

    private ClickGuiFeature getClickGuiFeature() {
        return FEATURE_SERVICE.getStorage().getByClass(ClickGuiFeature.class);
    }

    private void initPanels() {
        FeatureCategory hudCategory = FeatureCategory.of("HUD");
        Point pos = new Point(20, 20);
        categoryPositions.put(hudCategory, pos);

        if (!categoryPanels.containsKey(hudCategory)) {
            categoryPanels.put(hudCategory, new CategoryPanel(hudCategory));
        }
    }

    @Override
    public void renderBackground(GuiGraphics context, int i, int j, float f) {
        ClickGuiFeature clickGui = getClickGuiFeature();
        if (MC.level != null && clickGui != null && clickGui.blur.get()) {
            this.renderBlurredBackground(context);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int scaledMouseX = (int) (mouseX / CLICK_GUI_SCREEN.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI_SCREEN.scale);

        ClickGuiFeature clickGuiFeature = getClickGuiFeature();
        if (clickGuiFeature != null && clickGuiFeature.background.get()) {
            renderMenuBackground(context);
/*            int alpha = (clickGuiFeature.backgroundAlpha.get() & 0xFF) << 24;
            int color = alpha | (Feature_SERVICE.getStorage().getByClass(ColorFeature.class).getStyledGlobalColor().getRGB() & 0xFFFFFF);
            context.fill(0, 0, this.width, this.height, CLICK_GUI.applyFade(color));*/
        }

        NAVIGATE_PANEL.render(context, FONT_SERVICE.rendererProvider.getRenderer(), mouseX, mouseY);


        context.pose().pushMatrix();
        context.pose().scale(CLICK_GUI_SCREEN.scale, CLICK_GUI_SCREEN.scale);

        FeatureCategory hudCategory = FeatureCategory.of("HUD");
        Point pos = categoryPositions.get(hudCategory);
        CategoryPanel hudPanel = categoryPanels.get(hudCategory);

        if (pos != null && hudPanel != null) {
            hudPanel.render(context, FONT_SERVICE.rendererProvider.getRenderer(), pos.x, pos.y, scaledMouseX, scaledMouseY, this.height);
        }

        if (hudPanel != null && clickGuiFeature != null && clickGuiFeature.descriptions.get() && pos != null) {
            double scrollOffset = hudPanel.getScrollOffset();
            List<Feature> Features = FEATURE_SERVICE.getStorage().getByCategory(hudCategory);

            int curY = pos.y + CategoryPanel.HEADER_HEIGHT + FeaturePanel.Feature_SPACING + CategoryPanel.BOTTOM_MARGIN
                    - (int) scrollOffset;

            for (Feature Feature : Features) {
                int modX = pos.x + CategoryPanel.BORDER_WIDTH + SettingPanel.INNER_PADDING;

                if (FeaturePanel.isHovered(scaledMouseX, scaledMouseY, modX, curY)) {
                    String description = Feature.getDescription();
                    if (description != null && !description.isEmpty()) {
                        int descX = scaledMouseX + 5;
                        int descY = scaledMouseY;
                        int textWidth = FONT_SERVICE.getWidth(Component.nullToEmpty(description));
                        int textHeight = 8;

                        context.fill(descX - 2, descY - 2, descX + textWidth + 2, descY + textHeight + 2, 0x7F000000);
                        FONT_SERVICE.drawText(context, description, descX, descY, CLICK_GUI_SCREEN.applyFade(FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class).getStyledTextColor(255).getRGB()), true);
                    }
                    context.pose().popMatrix();

                    renderHudElements(context, mouseX, mouseY);

                    super.render(context, mouseX, mouseY, delta);
                    return;
                }

                curY += FeaturePanel.HEIGHT + FeaturePanel.Feature_SPACING;
                if (Feature.isExpanded()) {
                    curY += SettingPanel.getSettingsHeight(Feature);
                }
            }
        }

        context.pose().popMatrix();

        renderHudElements(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderHudElements(GuiGraphics context, int mouseX, int mouseY) {
        int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();
        int screenHeight = MC.getWindow().getGuiScaledHeight();
        int chatZoneTop = screenHeight - (screenHeight / 8);

        for (Feature Feature : FEATURE_SERVICE.getStorage().getByCategory(FeatureCategory.of("HUD"))) {
            if (Feature instanceof HudElementFeature hud && hud.isEnabled()) {
                int y = hud.getRenderY();
                int renderY = (y + hud.height >= chatZoneTop) ? y - chatAnimationOffset : y;
                int baseX = hud.getRenderX();

                boolean hovered = mouseX >= baseX && mouseX <= baseX + hud.width &&
                        mouseY >= renderY && mouseY <= renderY + hud.height;

                if (hovered) {
                    context.fill(baseX - 1, renderY - 1, baseX + hud.width + 1, renderY + hud.height + 1, 0x50FFFFFF);
                }

                for (HudElementFeature.TextElement element : new ArrayList<>(hud.getTextElements())) {
                    int drawX = hud.getRenderXForElement(element);
                    int drawY = renderY + element.offsetY();
                    FONT_SERVICE.drawText(context, element.text(), drawX, drawY, true);
                }

                hud.renderItems(context);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean bl) {
        int scaledMouseX = (int) (click.x() / CLICK_GUI_SCREEN.scale);
        int scaledMouseY = (int) (click.y() / CLICK_GUI_SCREEN.scale);

        NAVIGATE_PANEL.mouseClicked(click.x(), click.y(), FONT_SERVICE.rendererProvider.getRenderer());

        FeatureCategory hudCategory = FeatureCategory.of("HUD");
        Point pos = categoryPositions.get(hudCategory);
        CategoryPanel hudPanel = categoryPanels.get(hudCategory);

        if (pos != null && hudPanel != null && CategoryPanel.isHeaderHovered(scaledMouseX, scaledMouseY, pos.x, pos.y)) {
            if (click.button() == 0) {
                playClickSound();
                draggingCategory = true;
                draggedFeatureCategory = hudCategory;
                dragStartX = scaledMouseX;
                dragStartY = scaledMouseY;
                initialCategoryX = pos.x;
                initialCategoryY = pos.y;
                return true;
            }
        }

        if (!draggingCategory && pos != null && hudPanel != null) {
            double scrollOffset = hudPanel.getScrollOffset();
            List<Feature> Features = FEATURE_SERVICE.getStorage().getByCategory(hudCategory);

            int curY = pos.y + CategoryPanel.HEADER_HEIGHT + FeaturePanel.Feature_SPACING + CategoryPanel.BOTTOM_MARGIN
                    - (int) scrollOffset;

            for (Feature Feature : Features) {
                int modX = pos.x + CategoryPanel.BORDER_WIDTH + SettingPanel.INNER_PADDING;

                if (FeaturePanel.isHovered(scaledMouseX, scaledMouseY, modX, curY)) {
                    if (click.button() == 0) {
                        playClickSound();
                        Feature.toggle();
                    } else if (click.button() == 1) {
                        if (Feature.isExpanded())
                            Feature.setExpanded(false);
                        else Feature.setExpanded(true);
                        playClickSound();
                    } else if (click.button() == 2) {
                        playClickSound();
                        Feature.setDrawn(!Feature.isDrawn());
                    }
                    return true;
                }

                curY += FeaturePanel.HEIGHT + FeaturePanel.Feature_SPACING;
                if (Feature.isExpanded()) {
                    if (SettingPanel.mouseClicked(Feature, scaledMouseX, scaledMouseY, click.button(), modX, curY)) return true;
                    curY += SettingPanel.getSettingsHeight(Feature);
                }
            }
        }

        if (click.button() == 0) {
            int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();
            int screenHeight = MC.getWindow().getGuiScaledHeight();
            int chatZoneTop = screenHeight - (screenHeight / 8);

            for (Feature Feature : FEATURE_SERVICE.getStorage().getByCategory(FeatureCategory.of("HUD"))) {
                if (Feature instanceof HudElementFeature hud && hud.isEnabled()) {
                    int x = hud.getRenderX();
                    int y = hud.getRenderY();
                    int renderY = (y + hud.height >= chatZoneTop) ? y - chatAnimationOffset : y;

                    if (click.x() >= x && click.x() <= x + hud.width &&
                            click.y() >= renderY && click.y() <= renderY + hud.height) {
                        draggingElement = hud;
                        dragOffsetX = (int) click.x() - x;
                        dragOffsetY = (int) click.y() - renderY;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double d, double e) {
        int scaledMouseX = (int) (click.x() / CLICK_GUI_SCREEN.scale);
        int scaledMouseY = (int) (click.y() / CLICK_GUI_SCREEN.scale);

        if (draggingCategory && draggedFeatureCategory != null) {
            Point pos = categoryPositions.get(draggedFeatureCategory);
            if (pos != null) {
                pos.x = initialCategoryX + (scaledMouseX - dragStartX);
                pos.y = initialCategoryY + (scaledMouseY - dragStartY);
                return true;
            }
        }

        if (click.button() == 0 && draggingElement != null) {
            dragHudElement(click.x(), click.y());
            return true;
        }

        SettingPanel.mouseDragged(scaledMouseX, scaledMouseY);
        return super.mouseDragged(click, d, e);
    }

    private void dragHudElement(double mouseX, double mouseY) {
        int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();
        int newRenderX = (int) mouseX - dragOffsetX;
        int newRenderY = (int) (mouseY - dragOffsetY + chatAnimationOffset);

        int screenWidth = MC.getWindow().getGuiScaledWidth();
        int screenHeight = MC.getWindow().getGuiScaledHeight();

        newRenderY = Math.max(1, Math.min(newRenderY, screenHeight - draggingElement.height - 1));

        int newX;
        switch (draggingElement.alignment.get()) {
            case LEFT -> {
                newRenderX = Math.max(1, Math.min(newRenderX, screenWidth - draggingElement.width - 1));
                newX = newRenderX;
            }
            case CENTER -> {
                newRenderX = Math.max(draggingElement.width / 2, Math.min(newRenderX, screenWidth - draggingElement.width / 2));
                newX = newRenderX + draggingElement.width / 2;
            }
            case RIGHT -> {
                newRenderX = Math.max(0, Math.min(newRenderX, screenWidth - draggingElement.width));
                newX = newRenderX + draggingElement.width;
            }
            default -> newX = Math.max(1, Math.min(newRenderX, screenWidth - draggingElement.width - 1));
        }

        boolean intersects = false;
        for (Feature Feature : FEATURE_SERVICE.getStorage().getByCategory(FeatureCategory.of("HUD"))) {
            if (Feature instanceof HudElementFeature other && other.isEnabled() && other != draggingElement) {
                boolean overlapX = newRenderX < other.getRenderX() + other.width && newRenderX + draggingElement.width > other.getRenderX();
                boolean overlapY = newRenderY < other.getRenderY() + other.height && newRenderY + draggingElement.height > other.getRenderY();
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
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int scaledMouseX = (int) (mouseX / CLICK_GUI_SCREEN.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI_SCREEN.scale);

        for (FeatureCategory FeatureCategory : FeatureCategory.getAll()) {
            if (!"hud".equalsIgnoreCase(FeatureCategory.getName())) continue;

            Point pos = categoryPositions.get(FeatureCategory);
            if (pos == null) continue;

            CategoryPanel panel = categoryPanels.get(FeatureCategory);
            if (panel != null && panel.mouseScrolled(scaledMouseX, scaledMouseY, verticalAmount, pos.x, pos.y, this.height)) {
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        draggingCategory = false;
        draggedFeatureCategory = null;

        if (click.button() == 0) draggingElement = null;

        SettingPanel.mouseReleased(click);
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyEvent keyInput) {
        if (keyInput.input() == FEATURE_SERVICE.getStorage().getByClass(ClickGuiFeature.class).getKeyBind().get() && MC.level != null) {
            MC.setScreen(null);
            return true;
        }
        if (SettingPanel.keyPressed(keyInput.input())) return true;
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void playClickSound() {
        MC.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0f
        ));
    }
}
