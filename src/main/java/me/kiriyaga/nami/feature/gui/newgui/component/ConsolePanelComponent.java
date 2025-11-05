package me.kiriyaga.nami.feature.gui.newgui.component;

import me.kiriyaga.nami.feature.gui.newgui.base.DataPanelComponent;
import me.kiriyaga.nami.feature.gui.newgui.widget.ButtonWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

import static me.kiriyaga.nami.Nami.*;

public class ConsolePanelComponent extends DataPanelComponent<Text> {
    private final StringBuilder inputBuffer = new StringBuilder();

    private final Consumer<Text> onAdd;
    private final Consumer<Text> onRemove;
    private final Consumer<Text> onClick;

    private final ButtonWidget enterButton;
    private boolean inputFocused = false;

    public ConsolePanelComponent(String name, int x, int y, int width, int height,
                                 Consumer<Text> onAdd, Consumer<Text> onRemove, Consumer<Text> onClick) {
        super(name, x, y, width, height, t -> t);
        this.onAdd = onAdd;
        this.onRemove = onRemove;
        this.onClick = onClick;

        this.enterButton = new ButtonWidget(
                "Enter",
                x + width - 2 - 50,
                y + height - 2 - inputHeight,
                50,
                inputHeight,
                true,
                this::submitInput
        );
    }

    private void submitInput() {
        String text = inputBuffer.toString().trim();
        if (!text.isEmpty() && onAdd != null) {
            onAdd.accept(Text.literal(text));
            inputBuffer.setLength(0);
        }
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        super.render(context, textRenderer, mouseX, mouseY);

        enterButton.setPosition(
                x + width - 2 - enterButton.getWidth(),
                y + height - 2 - enterButton.getHeight()
        );

        renderInputBox(context, textRenderer);
        enterButton.render(context, textRenderer, mouseX, mouseY);
    }

    private void renderInputBox(DrawContext context, TextRenderer textRenderer) {
        int inputY = y + height - inputHeight - 2;
        int inputX = x + 2;
        int inputBg = inputFocused ? 0x80202020 : 0x80101010;
        int inputWidth = enterButton.getX() - inputX - 1;

        context.fill(inputX, inputY, inputX + inputWidth, inputY + inputHeight, inputBg);
        String inputStr = inputBuffer.toString() +
                (inputFocused && (System.currentTimeMillis() / 500 % 2 == 0) ? "_" : "");
        FONT_MANAGER.drawText(context, inputStr, inputX + 2, inputY + 4, 0xFFFFFFFF, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (enterButton.mouseClicked((int) mouseX, (int) mouseY, button)) {
            inputFocused = false;
            return true;
        }

        int inputY = y + height - inputHeight - 2;
        boolean insideInput = mouseX >= x && mouseX <= x + width - 60 &&
                mouseY >= inputY && mouseY <= inputY + inputHeight;

        if (insideInput && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            inputFocused = true;
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !insideInput)
            inputFocused = false;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY) {
        boolean result = super.mouseDragged(mouseX, mouseY, deltaX, deltaY);
        if (result) {
            enterButton.setPosition(
                    x + width - 2 - enterButton.getWidth(),
                    y + height - 2 - enterButton.getHeight()
            );
        }
        return result;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!inputFocused) return false;
        if (keyCode == GLFW.GLFW_KEY_ENTER) { submitInput(); inputFocused = false; return true; }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { inputBuffer.setLength(0); inputFocused = false; return true; }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && inputBuffer.length() > 0) {
            inputBuffer.deleteCharAt(inputBuffer.length() - 1);
            return true;
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!inputFocused) return false;
        if (chr >= 32 && chr != 127) { inputBuffer.append(chr); return true; }
        return false;
    }
}
