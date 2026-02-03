package namidevelopment.kiriyaga.nami.impl.gui.oldgui.settings;

import namidevelopment.kiriyaga.api.client.ColorFeature;
import namidevelopment.kiriyaga.api.model.setting.KeyBindSetting;
import namidevelopment.kiriyaga.api.util.KeyUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static namidevelopment.kiriyaga.api.util.ColorUtils.toRGBA;
import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;
public class KeyBindSettingRenderer implements SettingRenderer<KeyBindSetting> {
    private static KeyBindSetting waitingForKeyBind = null;

    @Override
    public void render(GuiGraphics context, Font textRenderer, KeyBindSetting setting, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);
        Color textCol = getColorFeature().getStyledTextColor(255);
        Color bgColor = new Color(30, 30, 30, 0);

        int bgColorInt = CLICK_GUI_SCREEN.applyFade(toRGBA(bgColor));
        int textColorInt = CLICK_GUI_SCREEN.applyFade(toRGBA(textCol));

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);

        int textX = x + PADDING + (hovered ? 1 : 0);
        int textY = y + (HEIGHT - 8) / 2;

        String nameStr = hovered
                ? (setting.isHoldMode() ? "Hold" : "Toggle")
                : setting.getName();

        FONT_SERVICE.drawText(
                context,
                nameStr,
                textX,
                textY,
                textColorInt,
                true
        );

        String valueStr;
        if (waitingForKeyBind == setting) {
            valueStr = "Listening...";
        } else {
            String keyName = KeyUtils.getKeyName(setting.get());
            //valueStr = (setting.isHoldMode() ? "hold: " : "toggle: ") + keyName;
            //setting.setName(setting.isHoldMode() ? "hold" : "toggle"); // yes unfortunatelly
            valueStr = keyName;
        }

        String renderStr = valueStr;
        int textWidth = FONT_SERVICE.getWidth(renderStr);
        int valueX = x + WIDTH - PADDING - textWidth;

        FONT_SERVICE.drawText(
                context,
                renderStr,
                valueX,
                textY,
                textColorInt,
                true
        );
    }

    @Override
    public boolean mouseClicked(KeyBindSetting setting, double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, (int) mouseX, (int) mouseY)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                setting.setHoldMode(!setting.isHoldMode());
                return true;
            }
        }

        if (waitingForKeyBind == null) {
            waitingForKeyBind = setting;
        } else if (waitingForKeyBind == setting) {
            waitingForKeyBind.set(button);
            waitingForKeyBind = null;
        }
        return true;
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

    private ColorFeature getColorFeature() {
        return FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class);
    }
}