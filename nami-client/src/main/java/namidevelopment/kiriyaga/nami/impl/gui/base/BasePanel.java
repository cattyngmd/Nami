package namidevelopment.kiriyaga.nami.impl.gui.base;

import namidevelopment.kiriyaga.api.util.ColorUtils;
import namidevelopment.kiriyaga.nami.impl.feature.client.ColorFeature;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

import static namidevelopment.kiriyaga.api.NamiApi.FEATURE_SERVICE;
import static namidevelopment.kiriyaga.api.NamiApi.FONT_SERVICE;
import static namidevelopment.kiriyaga.api.util.ColorUtils.toRGBA;

public abstract class BasePanel {

    protected int x;
    protected int y;
    protected int width;
    public int height;

    protected static final int PADDING = 3;

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }

    protected ColorFeature getColorFeature() {
        return FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class);
    }

    protected abstract String getName();
    protected abstract boolean isEnabled();

    protected Color getTextColor() {
        return new Color(255, 255, 255, 255);
    }

    protected Color getEnabledColor() {
        return getColorFeature().getStyledGlobalColor();
    }

    protected Color getDisabledColor() {
        return getColorFeature().getStyledGlobalColor(30);
    }

    public void render(GuiGraphics context, Font font, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY);

        Color fillCol = isEnabled() ? getEnabledColor() : getDisabledColor();

        if (hovered) {
            fillCol = ColorUtils.brighten(fillCol, 20);
        }

        context.fill(x, y, x + width, y + height, toRGBA(fillCol));

        int textY = (y + (height - 8) / 2) + 1;
        int textX = x + PADDING + (hovered ? 1 : 0);

        FONT_SERVICE.drawText(context, getName(), textX, textY, toRGBA(getTextColor()), true);
    }

    public void onLeftClick() {}
    public void onRightClick() {}
    public void onMiddleClick() {}

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return false;
        switch (button) {
            case 0 -> onLeftClick();
            case 1 -> onRightClick();
            case 2 -> onMiddleClick();
        }
        return true;
    }
}
