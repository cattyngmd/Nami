package me.kiriyaga.nami.feature.gui.settings;

import me.kiriyaga.nami.feature.gui.components.CategoryPanel;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import java.awt.*;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.*;

public class BoolSettingRenderer implements SettingRenderer<BoolSetting> {

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, BoolSetting setting, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);
        ColorModule colorModule = getColorModule();
        Color primary = colorModule.getStyledGlobalColor();
        Color secondary = colorModule.getStyledSecondColor();
        Color textCol = new Color(255, 255, 255, 122);
        Color bgColor = new Color(30, 30, 30, 0);
        Color textColActivated = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).moduleFill.get()
                ? new Color(255, 255, 255, 255)
                : new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);

        int bgColorInt = toRGBA(bgColor);
        int textColorInt = setting.get() ? toRGBA(textColActivated) : toRGBA(textCol);

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);

        int lineOffset = 1;
        if (MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).expandedIdentifier.get())
            context.fill(
                x,
                y - lineOffset,
                x + 1,
                y + HEIGHT,
                setting.getParentModule().isEnabled() ? primary.getRGB() : secondary.getRGB()
            );


        int textX = x + PADDING + (hovered ? 1 : 0);
        int textY = y + (HEIGHT - 8) / 2;

        context.drawText(textRenderer, setting.getName(), textX, textY, textColorInt, false);
    }

    @Override
    public boolean mouseClicked(BoolSetting setting, double mouseX, double mouseY, int button) {
        setting.toggle();
        return true;
    }

    @Override
    public void mouseDragged(BoolSetting setting, double mouseX) {
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    protected ColorModule getColorModule() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
    }
}