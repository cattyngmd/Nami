package me.kiriyaga.nami.impl.gui.oldgui.settings;

import me.kiriyaga.nami.impl.gui.oldgui.components.CategoryPanel;
import me.kiriyaga.nami.impl.gui.oldgui.components.SettingPanel;
import me.kiriyaga.nami.impl.setting.Setting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public interface SettingRenderer<T extends Setting<?>> {
    int HEIGHT = 13;
    int PADDING = 3;
    int WIDTH = 100 - CategoryPanel.BORDER_WIDTH * 2 - SettingPanel.INNER_PADDING * 2;
    int SLIDER_HEIGHT = 1;
    int Feature_SPACING = 1;

    void render(GuiGraphics context, Font textRenderer, T setting, int x, int y, int mouseX, int mouseY);
    boolean mouseClicked(T setting, double mouseX, double mouseY, int button);
    void mouseDragged(T setting, double mouseX);
    default boolean mouseReleased(T setting, double mouseX, double mouseY, int button) {
        return false;
    }

    default int getHeight(T setting) {
        return HEIGHT;
    }
}
