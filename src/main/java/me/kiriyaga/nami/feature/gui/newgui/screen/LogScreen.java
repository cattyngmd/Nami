package me.kiriyaga.nami.feature.gui.newgui.screen;

import me.kiriyaga.nami.feature.gui.newgui.base.NamiScreen;
import me.kiriyaga.nami.feature.gui.newgui.component.ConsolePanelComponent;
import me.kiriyaga.nami.feature.gui.newgui.entry.LogEntry;
import me.kiriyaga.nami.feature.gui.newgui.widget.ActionItem;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

public class LogScreen extends NamiScreen {

    private ConsolePanelComponent<LogEntry> console;

    public LogScreen() {
        super(Text.literal("Log"));
    }

    private ClickGuiModule getClickGuiModule() {
        return MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
    }

    @Override
    protected void init() {
        super.init();

        if (console == null) {
            console = new ConsolePanelComponent<>(
                    "Log", 20, 20, 300, 200,
                    entry -> {},
                    this::removeEntry,
                    entry -> {},
                    LogEntry::new
            );
        }
    }

    @Override
    public void renderBackground(DrawContext context, int i, int j, float f) {
        if (MC.world != null && MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).blur.get())
            this.applyBlur(context);
    }

    public void addEntry(String text) {
        if (console != null) {
            console.addEntry(new LogEntry(text));
        }
    }

    private void removeEntry(LogEntry entry) {
        console.getEntries().remove(entry);
    }

    private LogEntry getEntryAt(double mouseX, double mouseY) {
        int contentY = console.getY() + console.getHeaderHeight() + 4;
        int lineHeight = textRenderer.fontHeight + 4;
        int contentHeight = console.getHeight() - console.getHeaderHeight() - console.getInputHeight() - 8;
        int maxVisible = contentHeight / lineHeight;
        double scroll = console.getScrollOffset();
        int start = (int) Math.floor(scroll);
        double partial = scroll - start;
        int drawY = contentY - (int)(partial * lineHeight);

        for (int i = start; i < Math.min(console.getEntries().size(), start + maxVisible + 1); i++) {
            LogEntry entry = console.getEntries().get(i);
            if (mouseY >= drawY && mouseY <= drawY + lineHeight) return entry;
            drawY += lineHeight;
        }
        return null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ClickGuiModule clickGuiModule = getClickGuiModule();
        if (clickGuiModule != null && clickGuiModule.background.get()) {
            int alpha = (clickGuiModule.backgroundAlpha.get() & 0xFF) << 24;
            int color = alpha | (MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor().getRGB() & 0xFFFFFF);context.fill(0, 0, width, height, CLICK_GUI.applyFade(color));
        }

        NAVIGATE_PANEL.render(context, textRenderer, mouseX, mouseY);

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(CLICK_GUI.scale, CLICK_GUI.scale);
        console.render(context, textRenderer, (int)(mouseX / CLICK_GUI.scale), (int)(mouseY / CLICK_GUI.scale));
        context.getMatrices().popMatrix();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        NAVIGATE_PANEL.mouseClicked(x, y, textRenderer);

        double sx = x / CLICK_GUI.scale;
        double sy = y / CLICK_GUI.scale;

        if (button == 1) {
            LogEntry entry = getEntryAt(sx, sy);
            if (entry != null) {
                console.getActionWidget().clearItems();
                console.getActionWidget().addItem(new ActionItem("delete", () -> removeEntry(entry)));
                console.getActionWidget().addItem(new ActionItem("copy", () -> MC.keyboard.setClipboard(entry.getRawMessage())));
                console.getActionWidget().setPosition((int)sx, (int)sy);
                console.getActionWidget().setVisible(true);
                return true;
            }
        }

        if (console.getActionWidget().isVisible() &&
                console.getActionWidget().mouseClicked(sx, sy, button)) return true;

        return console.mouseClicked(sx, sy, button) || super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double h, double v) {
        return console.mouseScrolled(x / CLICK_GUI.scale, y / CLICK_GUI.scale, v) || super.mouseScrolled(x, y, h, v);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return console.mouseDragged(x / CLICK_GUI.scale, y / CLICK_GUI.scale, dx, dy)
                || super.mouseDragged(x, y, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        console.mouseReleased(x / CLICK_GUI.scale, y / CLICK_GUI.scale, button);
        return super.mouseReleased(x, y, button);
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        return console.keyPressed(k, s, m) || super.keyPressed(k, s, m);
    }

    @Override
    public boolean charTyped(char c, int m) {
        return console.charTyped(c, m) || super.charTyped(c, m);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
