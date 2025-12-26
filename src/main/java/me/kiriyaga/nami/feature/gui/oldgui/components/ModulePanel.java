package me.kiriyaga.nami.feature.gui.oldgui.components;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import java.awt.*;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.ColorUtils.*;

public class ModulePanel {
    public static final int WIDTH = 100 - CategoryPanel.BORDER_WIDTH * 2 - SettingPanel.INNER_PADDING * 2;
    public static final int HEIGHT = 13;
    public static final int PADDING = 3;
    public static final int MODULE_SPACING = 1;

    private final Module module;

    private ColorModule getColorModule() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
    }

    public ModulePanel(Module module) {
        this.module = module;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);

        Color textPrimary = getColorModule().getStyledTextColor(255);
        Color textSecond = getColorModule().getStyledTextSecondColor(255);
        Color textCol = module.isEnabled() ? textPrimary : textSecond;
        Color primary = getColorModule().getStyledGlobalColor();
        Color second = getColorModule().getStyledSecondColor();
        Color fillCol = module.isEnabled() ? primary : second;

        if (MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).moduleFill.get())
            context.fill(x, y, x + WIDTH, y + HEIGHT, CLICK_GUI.applyFade(toRGBA(fillCol)));

        if (!MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).coloredText.get())
            textCol = new Color(255, 255, 255, 255);

        int textY = (y + (HEIGHT - 8) / 2 ) +1;
        int baseTextX = x + PADDING + (hovered ? 1 : 0);
        FONT_MANAGER.drawText(
                context,
                module.getName(),
                baseTextX,
                textY,
                CLICK_GUI.applyFade(toRGBA(textCol)),
                true
        );
    }

    public static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }
}