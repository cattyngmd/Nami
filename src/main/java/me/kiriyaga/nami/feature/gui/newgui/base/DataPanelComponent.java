package me.kiriyaga.nami.feature.gui.newgui.base;

import me.kiriyaga.nami.feature.gui.newgui.base.PanelRenderer;
import me.kiriyaga.nami.util.render.ScissorUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static me.kiriyaga.nami.Nami.*;

public class DataPanelComponent<T> {
    protected final List<T> entries = new ArrayList<>();
    protected final Function<T, Text> displayMapper;

    protected final PanelRenderer panelRenderer = new PanelRenderer();

    protected double scrollOffset = 0;
    protected double targetScrollOffset = 0;

    protected final int headerHeight = 20;
    protected final int inputHeight = 20;

    protected String name;
    protected int x, y, width, height;

    protected boolean dragging = false;
    protected int dragOffsetX = 0;
    protected int dragOffsetY = 0;

    public DataPanelComponent(String name, int x, int y, int width, int height, Function<T, Text> displayMapper) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.displayMapper = displayMapper;
    }

    public void setEntries(List<T> items) {
        entries.clear();
        entries.addAll(items);
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        panelRenderer.renderPanel(context, x, y, width, height, headerHeight);
        panelRenderer.renderHeaderText(context, textRenderer, name, x, y, headerHeight, 4);

        int contentY = y + headerHeight + 4;
        int lineHeight = textRenderer.fontHeight + 4;
        int contentHeight = height - headerHeight - inputHeight - 8;
        int maxVisible = contentHeight / lineHeight;

        scrollOffset += (targetScrollOffset - scrollOffset) * 0.3;
        double maxScroll = Math.max(0, entries.size() - maxVisible);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        int start = (int) Math.floor(scrollOffset);
        double partialOffset = scrollOffset - start;

        ScissorUtil.enable(context, x, contentY, x + width, contentY + contentHeight);
        int drawY = contentY - (int) (partialOffset * lineHeight);

        for (int i = start; i < Math.min(entries.size(), start + maxVisible + 1); i++) {
            T item = entries.get(i);
            Text display = displayMapper.apply(item);
            FONT_MANAGER.drawText(context, display, x + 4, drawY, 0xFFFFFFFF, false);
            drawY += lineHeight;
        }

        ScissorUtil.disable(context);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        int contentY = y + headerHeight + 4;
        int contentHeight = height - headerHeight - inputHeight - 8;

        if (mouseX >= x && mouseX <= x + width && mouseY >= contentY && mouseY <= contentY + contentHeight) {
            int lineHeight = FONT_MANAGER.getHeight() + 4;
            int maxVisible = contentHeight / lineHeight;
            double maxScroll = Math.max(0, entries.size() - maxVisible);

            targetScrollOffset -= scrollDelta * 1.5;
            targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxScroll));
            return true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHeaderHovered(mouseX, mouseY)) {
            dragging = true;
            dragOffsetX = (int) (mouseX - x);
            dragOffsetY = (int) (mouseY - y);
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (dragging) {
            x = (int) (mouseX - dragOffsetX);
            y = (int) (mouseY - dragOffsetY);
            return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    protected boolean isHeaderHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerHeight;
    }
}
