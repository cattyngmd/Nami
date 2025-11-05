package me.kiriyaga.nami.feature.gui.newgui.component;

import me.kiriyaga.nami.feature.gui.newgui.base.DataPanelComponent;
import me.kiriyaga.nami.feature.gui.newgui.entry.FriendEntry;
import me.kiriyaga.nami.feature.gui.newgui.widget.ButtonWidget;
import me.kiriyaga.nami.feature.gui.newgui.widget.TextBoxWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.Consumer;

import static me.kiriyaga.nami.Nami.*;

public class ConsolePanelComponent extends DataPanelComponent<FriendEntry> {
    private final Consumer<FriendEntry> onAdd;
    private final Consumer<FriendEntry> onRemove;
    private final Consumer<FriendEntry> onClick;

    private final ButtonWidget enterButton;
    private final TextBoxWidget inputBox;

    public ConsolePanelComponent(String name, int x, int y, int width, int height,
                                 Consumer<FriendEntry> onAdd, Consumer<FriendEntry> onRemove, Consumer<FriendEntry> onClick) {
        super(name, x, y, width, height, FriendEntry::getDisplayText);
        this.onAdd = onAdd;
        this.onRemove = onRemove;
        this.onClick = onClick;

        int inputY = y + height - inputHeight - 2;
        int inputX = x + 2;
        int inputWidth = width - 2 - 50 - 4;

        this.inputBox = new TextBoxWidget(inputX, inputY, inputWidth, inputHeight);
        this.enterButton = new ButtonWidget("Enter",
                x + width - 2 - 50,
                inputY,
                50,
                inputHeight,
                true,
                this::submitInput);

        this.inputBox.setOnEnter(this::submitInput);
    }

    private void submitInput() {
        String text = inputBox.getText().trim();
        if (!text.isEmpty() && onAdd != null) {
            FriendEntry entry = new FriendEntry(text);
            onAdd.accept(entry);
            inputBox.clear();
        }
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        super.render(context, textRenderer, mouseX, mouseY);

        inputBox.setPosition(x + 2, y + height - inputHeight - 2);
        enterButton.setPosition(x + width - 2 - enterButton.getWidth(), y + height - inputHeight - 2);

        inputBox.render(context, textRenderer, mouseX, mouseY);
        enterButton.render(context, textRenderer, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (enterButton.mouseClicked((int) mouseX, (int) mouseY, button)) return true;
        if (inputBox.mouseClicked((int) mouseX, (int) mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (inputBox.keyPressed(keyCode, scanCode, modifiers)) return true;
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (inputBox.charTyped(chr, modifiers)) return true;
        return false;
    }
}
