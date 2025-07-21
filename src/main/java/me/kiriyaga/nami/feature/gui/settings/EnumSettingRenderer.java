package me.kiriyaga.nami.feature.gui.settings;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.*;

public class EnumSettingRenderer implements SettingRenderer<EnumSetting<?>> {

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, EnumSetting<?> setting, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);
        Color primary = getColorModule().getStyledGlobalColor();
        Color textCol = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).moduleFill.get() ? new Color(255, 255, 255, 255) : new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);
        Color bgColor = new Color(30, 30, 30, 0);

        context.fill(x, y, x + WIDTH, y + HEIGHT, toRGBA(bgColor));

        float targetOffset = hovered ? 0f : -20f;

        int lineOffset = 1;
        context.fill(x - 1, y - lineOffset, x, y + HEIGHT, primary.getRGB());

        context.drawText(
                textRenderer,
                setting.getName(),
                (int)(x + PADDING),
                y + (HEIGHT - 8) / 2,
                toRGBA(textCol),
                false
        );

        String valueStr = setting.get().toString();
        context.drawText(
                textRenderer,
                valueStr,
                x + WIDTH - PADDING - textRenderer.getWidth(valueStr),
                y + (HEIGHT - 8) / 2,
                toRGBA(textCol),
                false
        );
    }

    @Override
    public boolean mouseClicked(EnumSetting<?> setting, double mouseX, double mouseY, int button) {
        setting.cycle();
        return true;
    }

    @Override
    public void mouseDragged(EnumSetting<?> setting, double mouseX) {
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    private ColorModule getColorModule() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
    }

    private float approach(float current, float target, float maxDelta) {
        if (current < target) {
            current += maxDelta;
            if (current > target) current = target;
        } else if (current > target) {
            current -= maxDelta;
            if (current < target) current = target;
        }
        return current;
    }
}
