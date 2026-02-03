package namidevelopment.kiriyaga.nami.impl.gui.oldgui.settings;

import namidevelopment.kiriyaga.api.client.ColorFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import java.awt.*;

import static namidevelopment.kiriyaga.api.util.ColorUtils.toRGBA;
import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;
public class BoolSettingRenderer implements SettingRenderer<BoolSetting> {

    @Override
    public void render(GuiGraphics context, Font textRenderer, BoolSetting setting, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);
        Color textCol = getColorFeature().getStyledTextSecondColor(255);
        Color bgColor = new Color(30, 30, 30, 0);
        Color textColActivated = getColorFeature().getStyledTextColor(255);

        int bgColorInt = CLICK_GUI_SCREEN.applyFade(toRGBA(bgColor));
        int textColorInt = CLICK_GUI_SCREEN.applyFade(setting.get() ? toRGBA(textColActivated) : toRGBA(textCol));

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);

        int lineOffset = 1;

        int textX = x + PADDING + (hovered ? 1 : 0);
        int textY = y + (HEIGHT - 8) / 2;

        FONT_SERVICE.drawText(context, setting.getName(), textX, textY, textColorInt, true);
    }

    @Override
    public boolean mouseClicked(BoolSetting setting, double mouseX, double mouseY, int button) {
        setting.toggle();
        return true;
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    protected ColorFeature getColorFeature() {
        return FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class);
    }
}