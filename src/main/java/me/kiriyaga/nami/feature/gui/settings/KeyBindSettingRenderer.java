package me.kiriyaga.nami.feature.gui.settings;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.setting.impl.KeyBindSetting;
import me.kiriyaga.nami.util.KeyUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import java.awt.*;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.*;

public class KeyBindSettingRenderer implements SettingRenderer<KeyBindSetting> {
    private static KeyBindSetting waitingForKeyBind = null;

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, KeyBindSetting setting, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);
        Color primary = getColorModule().getStyledGlobalColor();
        Color secondary = getColorModule().getStyledSecondColor();
        Color textCol = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).moduleFill.get()
                ? new Color(255, 255, 255, 255)
                : new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);
        Color bgColor = new Color(30, 30, 30, 0);

        context.fill(x, y, x + WIDTH, y + HEIGHT, toRGBA(bgColor));

        int lineOffset = 1;
        context.fill(
                x,
                y - lineOffset,
                x + 1,
                y + HEIGHT,
                setting.getParentModule().isEnabled() ? primary.getRGB() : secondary.getRGB()
        );

        int textX = x + PADDING + (hovered ? 1 : 0);
        int textY = y + (HEIGHT - 8) / 2;

        context.drawText(
                textRenderer,
                setting.getName().toLowerCase(),
                textX,
                textY,
                toRGBA(textCol),
                false
        );

        String valueStr = waitingForKeyBind == setting ? "Press a key..." : KeyUtils.getKeyName(setting.get());

        context.drawText(
                textRenderer,
                valueStr.toLowerCase(),
                x + WIDTH - PADDING - textRenderer.getWidth(valueStr),
                textY,
                toRGBA(textCol),
                false
        );
    }

    @Override
    public boolean mouseClicked(KeyBindSetting setting, double mouseX, double mouseY, int button) {
        if (waitingForKeyBind == null) {
            waitingForKeyBind = setting;
        } else if (waitingForKeyBind == setting) {
            waitingForKeyBind.set(button);
            waitingForKeyBind = null;
        }
        return true;
    }

    @Override
    public void mouseDragged(KeyBindSetting setting, double mouseX) {
    }

    public static boolean keyPressed(int keyCode) {
        if (waitingForKeyBind != null) {
            if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForKeyBind.set(-1);
            } else {
                waitingForKeyBind.set(keyCode);
            }
            waitingForKeyBind = null;
            return true;
        }
        return false;
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    private ColorModule getColorModule() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
    }
}