package me.kiriyaga.nami.feature.gui.base;

import me.kiriyaga.nami.feature.gui.base.PanelRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static me.kiriyaga.nami.Nami.*;

public class ConsolePanelRenderer {
    private final PanelRenderer panelRenderer = new PanelRenderer();

    private final List<String> entries = new ArrayList<>();

    private final Consumer<String> onAdd;
    private final Consumer<String> onRemove;
    private final Consumer<String> onClick;

    private boolean inputFocused = false;
    private StringBuilder inputBuffer = new StringBuilder();

    private int scrollOffset = 0;

    private final int x, y, width, height;
    private final int headerHeight = 14;
    private final int inputHeight = 14;

    public ConsolePanelRenderer(int x, int y, int width, int height,
                                    Consumer<String> onAdd,
                                    Consumer<String> onRemove,
                                    Consumer<String> onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onAdd = onAdd;
        this.onRemove = onRemove;
        this.onClick = onClick;
    }

    public void setEntries(List<String> items) {
        entries.clear();
        entries.addAll(items);
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        panelRenderer.renderPanel(context, x, y, width, height, headerHeight);
        panelRenderer.renderHeaderText(context, textRenderer, "Manager", x, y, headerHeight, 4);

        int contentY = y + headerHeight + 2;
        int contentHeight = height - headerHeight - inputHeight - 2;

        int lineHeight = textRenderer.fontHeight + 4;
        int maxVisible = contentHeight / lineHeight;

        int start = Math.max(0, entries.size() - maxVisible - scrollOffset);
        int end = Math.min(entries.size(), start + maxVisible);

        int drawY = contentY;
        for (int i = start; i < end; i++) {
            String entry = entries.get(i);

            FONT_MANAGER.drawText(context, entry, x + 4, drawY, 0xFFFFFFFF, false);
            int btnW = 20, btnH = lineHeight - 2;
            int btnX = x + width - btnW - 4;
            context.fill(btnX, drawY, btnX + btnW, drawY + btnH, 0x80AA0000);
            FONT_MANAGER.drawText(context, "X", btnX + 6, drawY + 2, 0xFFFFFFFF, false);

            drawY += lineHeight;
        }

        int btnX = x + width - 20;
        int btnWidth = 16;
        int btnHeight = 10;

        context.fill(btnX, contentY, btnX + btnWidth, contentY + btnHeight, 0x80000000);
        FONT_MANAGER.drawText(context, "UP", btnX + 2, contentY + 1, 0xFFFFFFFF, false);

        context.fill(btnX, contentY + contentHeight - btnHeight, btnX + btnWidth, contentY + contentHeight, 0x80000000);
        FONT_MANAGER.drawText(context, "DN", btnX + 2, contentY + contentHeight - btnHeight + 1, 0xFFFFFFFF, false);

        int inputY = y + height - inputHeight;
        int inputBg = inputFocused ? 0x80202020 : 0x80101010;
        context.fill(x, inputY, x + width, inputY + inputHeight, inputBg);

        String inputStr = inputBuffer.toString() + (inputFocused && (System.currentTimeMillis() / 500 % 2 == 0) ? "_" : "");
        FONT_MANAGER.drawText(context, inputStr, x + 4, inputY + 3, 0xFFFFFFFF, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int inputY = y + height - inputHeight;
        boolean insideInput = mouseX >= x && mouseX <= x + width && mouseY >= inputY && mouseY <= inputY + inputHeight;

        if (insideInput && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            inputFocused = true;
            return true;
        }

        int contentY = y + headerHeight + 2;
        int lineHeight = 12 + 4;
        int contentHeight = height - headerHeight - inputHeight - 2;
        int maxVisible = contentHeight / lineHeight;

        int start = Math.max(0, entries.size() - maxVisible - scrollOffset);
        int end = Math.min(entries.size(), start + maxVisible);

        int drawY = contentY;
        for (int i = start; i < end; i++) {
            int btnW = 20, btnH = lineHeight - 2;
            int btnX = x + width - btnW - 4;

            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= drawY && mouseY <= drawY + btnH) {
                if (onRemove != null) onRemove.accept(entries.get(i));
                return true;
            }
            drawY += lineHeight;
        }

        int btnX = x + width - 20;
        int btnWidth = 16;
        int btnHeight = 10;
        if (mouseX >= btnX && mouseX <= btnX + btnWidth) {
            if (mouseY >= contentY && mouseY <= contentY + btnHeight) {
                if (scrollOffset < entries.size()) scrollOffset++;
                return true;
            }
            if (mouseY >= contentY + contentHeight - btnHeight && mouseY <= contentY + contentHeight) {
                if (scrollOffset > 0) scrollOffset--;
                return true;
            }
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            inputFocused = false;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!inputFocused) return false;

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            String text = inputBuffer.toString().trim();
            if (!text.isEmpty()) {
                if (onAdd != null) onAdd.accept(text);
            }
            inputBuffer.setLength(0);
            inputFocused = false;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            inputBuffer.setLength(0);
            inputFocused = false;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (inputBuffer.length() > 0) {
                inputBuffer.deleteCharAt(inputBuffer.length() - 1);
            }
            return true;
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!inputFocused) return false;

        if (chr >= 32 && chr != 127) {
            inputBuffer.append(chr);
            return true;
        }
        return false;
    }
}
