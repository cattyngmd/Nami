package me.kiriyaga.nami.feature.gui.base;

import me.kiriyaga.nami.feature.gui.components.ButtonWidget;
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
    private final int headerHeight = 20;
    private final int inputHeight = 20;

    private ButtonWidget enterButton;

    public ConsolePanelRenderer(int x, int y, int width, int height, Consumer<String> onAdd, Consumer<String> onRemove, Consumer<String> onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onAdd = onAdd;
        this.onRemove = onRemove;
        this.onClick = onClick;

        this.enterButton = new ButtonWidget("Enter",
                x + width - 60, y + height - inputHeight + 2,
                50, inputHeight - 4, true,
                () -> {
                    String text = inputBuffer.toString().trim();
                    if (!text.isEmpty() && onAdd != null) {
                        onAdd.accept(text);
                        inputBuffer.setLength(0);
                    }
                });
    }

    public void setEntries(List<String> items) {
        entries.clear();
        entries.addAll(items);
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        panelRenderer.renderPanel(context, x, y, width, height, headerHeight);
        panelRenderer.renderHeaderText(context, textRenderer, "Console", x, y, headerHeight, 4);

        int contentY = y + headerHeight + 4;
        int contentHeight = height - headerHeight - inputHeight - 8;
        int lineHeight = textRenderer.fontHeight + 4;
        int maxVisible = contentHeight / lineHeight;

        int start = Math.max(0, entries.size() - maxVisible - scrollOffset);
        int end = Math.min(entries.size(), start + maxVisible);

        int drawY = contentY;
        for (int i = start; i < end; i++) {
            String entry = entries.get(i);
            FONT_MANAGER.drawText(context, entry, x + 4, drawY, 0xFFFFFFFF, false);
            drawY += lineHeight;
        }

        int inputY = y + height - inputHeight;
        int inputBg = inputFocused ? 0x80202020 : 0x80101010;
        context.fill(x, inputY, x + width - 60, inputY + inputHeight, inputBg);

        String inputStr = inputBuffer.toString() + (inputFocused && (System.currentTimeMillis() / 500 % 2 == 0) ? "_" : "");
        FONT_MANAGER.drawText(context, inputStr, x + 4, inputY + 4, 0xFFFFFFFF, false);

        enterButton.render(context, textRenderer, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int inputY = y + height - inputHeight;
        boolean insideInput = mouseX >= x && mouseX <= x + width - 60 && mouseY >= inputY && mouseY <= inputY + inputHeight;

        if (insideInput && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            inputFocused = true;
            return true;
        }

        if (enterButton.mouseClicked((int) mouseX, (int) mouseY, button)) {
            inputFocused = false;
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !insideInput) {
            inputFocused = false;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!inputFocused) return false;

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            String text = inputBuffer.toString().trim();
            if (!text.isEmpty() && onAdd != null) {
                onAdd.accept(text);
                inputBuffer.setLength(0);
            }
            inputFocused = false;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            inputBuffer.setLength(0);
            inputFocused = false;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && inputBuffer.length() > 0) {
            inputBuffer.deleteCharAt(inputBuffer.length() - 1);
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