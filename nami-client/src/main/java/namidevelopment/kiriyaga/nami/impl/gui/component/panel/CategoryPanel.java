package namidevelopment.kiriyaga.nami.impl.gui.component.panel;

import namidevelopment.kiriyaga.nami.impl.gui.base.BasePanel;
import namidevelopment.kiriyaga.nami.impl.gui.base.PanelRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {

    public static final int WIDTH = 100;
    public static final int HEADER_HEIGHT = 12;
    public static final int BORDER_WIDTH = 1;
    public static final int PADDING = 5;
    public static final int PANEL_SPACING = 1;
    private static final int INNER_PADDING = 1;

    private final String name;
    private final PanelRenderer renderer = new PanelRenderer();

    private final List<BasePanel> panels = new ArrayList<>();

    public CategoryPanel(String name) {
        this.name = name;
    }

    public void addPanel(BasePanel panel) {
        panels.add(panel);
    }

    public List<BasePanel> getPanels() {
        return panels;
    }

    public void render(GuiGraphics context, Font font, int x, int y, int mouseX, int mouseY) {
        int contentHeight = 0;

        for (BasePanel panel : panels) {
            contentHeight += panel.height + PANEL_SPACING;
        }

        contentHeight += INNER_PADDING * 2;

        int panelHeight = HEADER_HEIGHT + PANEL_SPACING + contentHeight;

        renderer.renderPanel(context, x, y, WIDTH, panelHeight, HEADER_HEIGHT);
        renderer.renderHeaderText(context, font, name, x, y, HEADER_HEIGHT, PADDING);

        int currentY = y + HEADER_HEIGHT + PANEL_SPACING + INNER_PADDING;

        for (BasePanel panel : panels) {
            panel.setBounds(x + BORDER_WIDTH + INNER_PADDING, currentY, WIDTH - BORDER_WIDTH * 2 - INNER_PADDING * 2, panel.height);
            panel.render(context, font, mouseX, mouseY);

            currentY += panel.height + PANEL_SPACING;
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        for (BasePanel panel : panels) {
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    public boolean isHeaderHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH &&
                mouseY >= y && mouseY <= y + HEADER_HEIGHT;
    }
}
