package me.kiriyaga.nami.feature.gui.newgui.screen;

import me.kiriyaga.nami.feature.gui.newgui.base.NamiScreen;
import me.kiriyaga.nami.feature.gui.newgui.entry.FriendEntry;
import me.kiriyaga.nami.feature.gui.newgui.widget.ActionItem;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.gui.newgui.component.ConsolePanelComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;
import java.util.stream.Collectors;

import static me.kiriyaga.nami.Nami.*;

public class FriendScreen extends NamiScreen {
    private ConsolePanelComponent console;

    public FriendScreen() {
        super(Text.literal("NamiFriends"));
    }

    @Override
    protected void init() {
        super.init();
        if (console == null) {
            console = new ConsolePanelComponent("Friends", 20, 20, 300, 200,
                    entry -> { FRIEND_MANAGER.addFriend(entry.getName()); updateEntries(); },
                    entry -> { FRIEND_MANAGER.removeFriend(entry.getName()); updateEntries(); },
                    entry -> {});
        }
        updateEntries();
    }

    private void updateEntries() {
        List<FriendEntry> friends = FRIEND_MANAGER.getFriends().stream()
                .map(FriendEntry::new)
                .collect(Collectors.toList());
        console.setEntries(friends);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ClickGuiModule clickGui = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
        updateEntries();

        NAVIGATE_PANEL.render(context, this.textRenderer, mouseX, mouseY);

        if (clickGui != null && clickGui.background.get()) {
            int alpha = (clickGui.backgroundAlpha.get() & 0xFF) << 24;
            int color = alpha | 0x101010;
            context.fill(0, 0, width, height, color);
        }

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(CLICK_GUI.scale, CLICK_GUI.scale);

        console.render(context, textRenderer, (int)(mouseX / CLICK_GUI.scale), (int)(mouseY / CLICK_GUI.scale));
        context.getMatrices().popMatrix();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        NAVIGATE_PANEL.mouseClicked(x, y, this.textRenderer);
        double scaledX = x / CLICK_GUI.scale;
        double scaledY = y / CLICK_GUI.scale;

        if (button == 1) {
            FriendEntry entry = getFriendAt(scaledX, scaledY);
            if (entry != null) {
                console.getActionWidget().clearItems();
                console.getActionWidget().addItem(new ActionItem("delete", () -> {
                    FRIEND_MANAGER.removeFriend(entry.getName());
                    updateEntries();
                }));

                console.getActionWidget().setPosition((int)scaledX, (int)scaledY);
                console.getActionWidget().setVisible(true);
                return true;
            }
        }

        if (console.getActionWidget().isVisible() &&
                console.getActionWidget().mouseClicked(scaledX, scaledY, button)) {
            return true;
        }

        return console.mouseClicked(scaledX, scaledY, button) || super.mouseClicked(x, y, button);
    }

    private FriendEntry getFriendAt(double mouseX, double mouseY) {
        int contentY = console.getY() + console.getHeaderHeight() + 4;
        int lineHeight = textRenderer.fontHeight + 4;
        int contentHeight = console.getHeight() - console.getHeaderHeight() - console.getInputHeight() - 8;
        int maxVisible = contentHeight / lineHeight;

        double scroll = console.getScrollOffset();
        int start = (int) Math.floor(scroll);
        double partial = scroll - start;
        int drawY = contentY - (int)(partial * lineHeight);

        for (int i = start; i < Math.min(console.getEntries().size(), start + maxVisible + 1); i++) {
            FriendEntry entry = console.getEntries().get(i);
            if (mouseY >= drawY && mouseY <= drawY + lineHeight) {
                return entry;
            }
            drawY += lineHeight;
        }
        return null;
    }

    @Override public boolean mouseScrolled(double x, double y, double h, double v) {
        return console.mouseScrolled(x / CLICK_GUI.scale, y / CLICK_GUI.scale, v) || super.mouseScrolled(x, y, h, v);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return console.mouseDragged(mouseX / CLICK_GUI.scale, mouseY / CLICK_GUI.scale, deltaX, deltaY)
                || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        console.mouseReleased(mouseX / CLICK_GUI.scale, mouseY / CLICK_GUI.scale, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override public boolean keyPressed(int k, int s, int m) {
        return console.keyPressed(k, s, m) || super.keyPressed(k, s, m);
    }

    @Override public boolean charTyped(char c, int m) {
        return console.charTyped(c, m) || super.charTyped(c, m);
    }

    @Override public boolean shouldPause() { return false; }
}
