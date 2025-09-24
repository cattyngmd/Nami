package me.kiriyaga.nami.feature.gui.base;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

import static me.kiriyaga.nami.Nami.CLICK_GUI;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.toRGBA;

public class PanelRenderer {

    private final ColorModule colorModule;
    private final ClickGuiModule clickGuiModule;

    public PanelRenderer() {
        this.colorModule = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
        this.clickGuiModule = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
    }

    public void renderPanel(DrawContext context, int x, int y, int width, int height, int headerHeight) {
        Color primary = colorModule.getStyledGlobalColor();

        int bgColor = CLICK_GUI.applyFade(toRGBA(new Color(30, 30, 30, clickGuiModule.guiAlpha.get()))
        );
        context.fill(x, y, x + width, y + height, bgColor);

        if (clickGuiModule.lines.get()) {
            int lineColor = CLICK_GUI.applyFade(primary.getRGB());
            context.fill(x, y + headerHeight, x + width, y + headerHeight + 1, lineColor);
            context.fill(x, y + height - 1, x + width, y + height, lineColor);
            context.fill(x, y + headerHeight + 1, x + 1, y + height - 1, lineColor);
            context.fill(x + width - 1, y + headerHeight + 1, x + width, y + height - 1, lineColor);
        } else {
            int lineColor = CLICK_GUI.applyFade(new Color(20, 20, 20, 122).getRGB());
            context.fill(x, y + headerHeight, x + width, y + headerHeight + 1, lineColor);
            context.fill(x, y + height - 1, x + width, y + height, lineColor);
            context.fill(x, y + headerHeight + 1, x + 1, y + height - 1, lineColor);
            context.fill(x + width - 1, y + headerHeight + 1, x + width, y + height - 1, lineColor);
        }

        context.fill(x + 1, y + headerHeight + 1, x + 2, y + height - 1, CLICK_GUI.applyFade(new Color(20, 20, 20, 122).getRGB()));
        context.fill(x + width - 2, y + headerHeight + 1, x + width - 1, y + height - 1, CLICK_GUI.applyFade(new Color(20, 20, 20, 122).getRGB()));

        context.fill(x, y, x + width, y + headerHeight, CLICK_GUI.applyFade(toRGBA(primary)));

        context.fill(x + 2, y + headerHeight + 1, x + width - 2, y + headerHeight + 2, CLICK_GUI.applyFade(new Color(20, 20, 20, 122).getRGB()));
    }

    public void renderHeaderText(DrawContext context, TextRenderer textRenderer, String text,
                                 int x, int y, int headerHeight, int padding) {
        Color primary = colorModule.getStyledGlobalColor();
        Color textCol = clickGuiModule.moduleFill.get() ? new Color(255, 255, 255, 255) : new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);

        int textY = y + (headerHeight - textRenderer.fontHeight) / 2;
        context.drawText(textRenderer, text, x + padding, textY + 1, CLICK_GUI.applyFade(toRGBA(textCol)), true);
    }
}
