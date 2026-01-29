package namidevelopment.kiriyaga.nami.impl.gui.oldgui.components;

import namidevelopment.kiriyaga.nami.impl.gui.newgui.base.PanelRenderer;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.impl.client.ClickGuiFeature;
import namidevelopment.kiriyaga.nami.util.render.ScissorUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.nami.impl.gui.oldgui.components.FeaturePanel.Feature_SPACING;

public class CategoryPanel {
    public static final int WIDTH = 100;
    public static final int HEADER_HEIGHT = 12;
    private static final int PADDING = 5;
    public static final int BORDER_WIDTH = 1;
    public static final int BOTTOM_MARGIN = 1;

    private final FeatureCategory FeatureCategory;
    private final PanelRenderer renderer = new PanelRenderer();

    private double scrollOffset = 0;
    private double targetScrollOffset = 0;

    public CategoryPanel(FeatureCategory FeatureCategory) {
        this.FeatureCategory = FeatureCategory;
    }

    public void render(GuiGraphics context, Font textRenderer, int x, int y, int mouseX, int mouseY, int screenHeight) {

        List<Feature> Features = FEATURE_SERVICE.getStorage().getByCategory(FeatureCategory);

        int dynamicContentHeight = 0;
        for (Feature Feature : Features) {
            dynamicContentHeight += FeaturePanel.HEIGHT + Feature_SPACING;
            if (Feature.isExpanded()) {
                dynamicContentHeight += SettingPanel.getSettingsHeight(Feature);
            }
        }

        int fullUnclampedHeight = HEADER_HEIGHT + BOTTOM_MARGIN + Feature_SPACING + dynamicContentHeight + Feature_SPACING;

        int maxAllowedHeight = screenHeight - y - 1;
        int basePanelHeight = Math.min(fullUnclampedHeight, maxAllowedHeight);

        renderer.renderPanel(context, x, y, WIDTH, basePanelHeight, HEADER_HEIGHT);
        renderer.renderHeaderText(context, textRenderer, FeatureCategory.getName(), x, y, HEADER_HEIGHT, PADDING);
        int contentY = y + HEADER_HEIGHT + Feature_SPACING + BOTTOM_MARGIN;
        int visibleHeight = Math.min(basePanelHeight - HEADER_HEIGHT - Feature_SPACING - BOTTOM_MARGIN, screenHeight - contentY - 1);

        boolean anyExpanded = Features.stream().anyMatch(Feature::isExpanded);
        if (anyExpanded) {
            visibleHeight -= 1;
            if (visibleHeight < 0) visibleHeight = 0;
        }

        int scrollableHeight = 0;
        for (Feature Feature : Features) {
            scrollableHeight += FeaturePanel.HEIGHT + Feature_SPACING;
            if (Feature.isExpanded()) {
                scrollableHeight += SettingPanel.getSettingsHeight(Feature);
            }
        }

        scrollOffset += (targetScrollOffset - scrollOffset) * 0.1;
        double maxScroll = Math.max(0, scrollableHeight - visibleHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        ScissorUtil.enable(context, x, contentY, x + WIDTH, contentY + visibleHeight);

        int FeatureY = contentY - (int) scrollOffset;

        for (Feature Feature : Features) {

            FeaturePanel FeaturePanel = new FeaturePanel(Feature);

            int FeatureX = x + BORDER_WIDTH + SettingPanel.INNER_PADDING;
            int panelOffset = 1;
            int startY = FeatureY;

            int expandedHeight = 0;
            if (Feature.isExpanded()) {
                expandedHeight = SettingPanel.getSettingsHeight(Feature);
            }

            if (Feature.isExpanded()) {
                int panelX = FeatureX - panelOffset + 1;
                int panelY = startY - panelOffset + 1;
                int panelHeight = FeaturePanel.HEIGHT + expandedHeight + (panelOffset * 2) - 1;
                int panelWidth = WIDTH - (BORDER_WIDTH + SettingPanel.INNER_PADDING) * 2 + panelOffset * 2 - 2;

                if (FEATURE_SERVICE.getStorage().getByClass(ClickGuiFeature.class).panels.get())
                    renderer.renderPanel(context, panelX, panelY, panelWidth, panelHeight, 0, false, true);
            }

            FeaturePanel.render(context, textRenderer, FeatureX, FeatureY, mouseX, mouseY);

            FeatureY += FeaturePanel.HEIGHT + Feature_SPACING;

            if (Feature.isExpanded()) {
                SettingPanel.renderSettings(context, textRenderer, Feature, FeatureX, FeatureY, mouseX, mouseY);
                FeatureY += expandedHeight;
            }
        }
        ScissorUtil.disable(context);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta, int x, int y, int screenHeight) {
        List<Feature> Features = FEATURE_SERVICE.getStorage().getByCategory(FeatureCategory);

        int dynamicContentHeight = 0;
        for (Feature Feature : Features) {
            dynamicContentHeight += FeaturePanel.HEIGHT + Feature_SPACING;
            if (Feature.isExpanded()) {
                dynamicContentHeight += SettingPanel.getSettingsHeight(Feature);
            }
        }

        int fullUnclampedHeight = HEADER_HEIGHT + BOTTOM_MARGIN + Feature_SPACING + dynamicContentHeight + Feature_SPACING;
        int maxAllowedHeight = screenHeight - y - 1;
        int basePanelHeight = Math.min(fullUnclampedHeight, maxAllowedHeight);

        int contentY = y + HEADER_HEIGHT + Feature_SPACING + BOTTOM_MARGIN;

        int visibleHeight = Math.min(basePanelHeight - HEADER_HEIGHT - Feature_SPACING - BOTTOM_MARGIN,
                screenHeight - contentY - 1);

        boolean anyExpanded = Features.stream().anyMatch(Feature::isExpanded);
        if (anyExpanded) {
            visibleHeight -= 1;
            if (visibleHeight < 0) visibleHeight = 0;
        }

        if (mouseX >= x && mouseX <= x + WIDTH &&
                mouseY >= contentY && mouseY <= contentY + visibleHeight) {

            targetScrollOffset -= scrollDelta * 45;

            double maxScroll = Math.max(0, dynamicContentHeight - visibleHeight);
            if (targetScrollOffset < 0) targetScrollOffset = 0;
            if (targetScrollOffset > maxScroll) targetScrollOffset = maxScroll;

            return true;
        }

        return false;
    }


    public boolean isMouseOverContent(double mouseX, double mouseY, int x, int y, int screenHeight) {
        List<Feature> Features = FEATURE_SERVICE.getStorage().getByCategory(FeatureCategory);

        int dynamicContentHeight = 0;
        for (Feature Feature : Features) {
            dynamicContentHeight += FeaturePanel.HEIGHT + Feature_SPACING;
            if (Feature.isExpanded()) {
                dynamicContentHeight += SettingPanel.getSettingsHeight(Feature);
            }
        }

        int fullUnclampedHeight = HEADER_HEIGHT + BOTTOM_MARGIN + Feature_SPACING + dynamicContentHeight + Feature_SPACING;
        int maxAllowedHeight = screenHeight - y - 1;
        int basePanelHeight = Math.min(fullUnclampedHeight, maxAllowedHeight);

        int contentY = y + HEADER_HEIGHT + Feature_SPACING + BOTTOM_MARGIN;

        int visibleHeight = Math.min(basePanelHeight - HEADER_HEIGHT - Feature_SPACING - BOTTOM_MARGIN,
                screenHeight - contentY - 1);

        boolean anyExpanded = Features.stream().anyMatch(Feature::isExpanded);
        if (anyExpanded) {
            visibleHeight -= 1;
            if (visibleHeight < 0) visibleHeight = 0;
        }

        return mouseX >= x && mouseX <= x + WIDTH &&
                mouseY >= contentY && mouseY <= contentY + visibleHeight;
    }

    public static boolean isHeaderHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
    }

    public double getScrollOffset() {
        return scrollOffset;
    }
}
