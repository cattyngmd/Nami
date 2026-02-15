package namidevelopment.kiriyaga.nami.impl.gui.component.panel.settings;

import namidevelopment.kiriyaga.api.model.setting.WhitelistSetting;
import namidevelopment.kiriyaga.nami.impl.feature.client.ColorFeature;
import namidevelopment.kiriyaga.nami.impl.gui.base.BasePanel;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

import static namidevelopment.kiriyaga.api.NamiApi.FONT_SERVICE;
import static namidevelopment.kiriyaga.api.util.ColorUtils.toRGBA;

public class WhitelistSettingPanel extends BasePanel {

    public static final int HEIGHT = 13;
    private static final int PADDING = 3;

    private final WhitelistSetting setting;

    public WhitelistSettingPanel(WhitelistSetting setting) {
        this.setting = setting;
        this.height = HEIGHT;
    }

    @Override
    public void render(GuiGraphics context, Font font, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY);

        Color textColor = getTextColor();
        int textY = y + (height - 8) / 2 + 1;
        int textX = x + PADDING + (hovered ? 1 : 0);

        FONT_SERVICE.drawText(context, getName(), textX, textY, toRGBA(textColor), true);
    }

    @Override
    public void onLeftClick() {
        setting.toggle();
    }

    @Override
    protected String getName() {
        return setting.getName();
    }

    @Override
    protected boolean isEnabled() {
        return setting.get();
    }

    @Override
    protected Color getTextColor() {
        ColorFeature colorFeature = getColorFeature();
        return isEnabled() ? colorFeature.getStyledTextColor(255) : colorFeature.getStyledTextSecondColor(255);
    }
}
