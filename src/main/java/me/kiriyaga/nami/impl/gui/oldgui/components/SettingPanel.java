package me.kiriyaga.nami.impl.gui.oldgui.components;

import me.kiriyaga.nami.impl.gui.oldgui.settings.*;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.setting.Setting;
import me.kiriyaga.nami.impl.setting.impl.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;
import java.util.Map;

import static me.kiriyaga.nami.Nami.MC;

public class SettingPanel {
    public static final int INNER_PADDING = 1;

    private static final Map<Setting<?>, SettingRenderer<?>> renderers = new HashMap<>(); // i hate myself

    private static Setting<?> draggedSetting = null;
    private static double dragStartX = 0;

    private static SettingRenderer<?> getRenderer(Setting<?> setting) { // wildcard??
        return renderers.computeIfAbsent(setting, s -> {
            if (s instanceof BoolSetting) return new BoolSettingRenderer();
            if (s instanceof ColorSetting) return new ColorSettingRenderer();
            if (s instanceof EnumSetting) return new EnumSettingRenderer();
            if (s instanceof IntSetting) return new IntSettingRenderer();
            if (s instanceof DoubleSetting) return new DoubleSettingRenderer();
            if (s instanceof KeyBindSetting) return new KeyBindSettingRenderer();
            if (s instanceof WhitelistSetting) return new WhitelistSettingRenderer();
            throw new IllegalArgumentException("Unknown setting type: " + s.getClass());
        });
    }

    public static int getSettingsHeight(Feature Feature) {
        int height = 0;
        for (Setting<?> setting : Feature.getSettings()) {
            if (!setting.isShow()) continue;
            SettingRenderer<Setting<?>> renderer = (SettingRenderer<Setting<?>>) getRenderer(setting);
            int settingHeight = renderer.getHeight(setting);
            height += settingHeight + SettingRenderer.Feature_SPACING;
        }
        return height;
    }

    public static int renderSettings(GuiGraphics context, Font textRenderer, Feature Feature, int x, int y, int mouseX, int mouseY) {
        int curY = y + SettingRenderer.Feature_SPACING;
        for (Setting<?> setting : Feature.getSettings()) {
            if (!setting.isShow()) continue;
            SettingRenderer<Setting<?>> renderer = (SettingRenderer<Setting<?>>) getRenderer(setting);
            renderer.render(context, textRenderer, setting, x, curY, mouseX, mouseY);
            curY += renderer.getHeight(setting) + SettingRenderer.Feature_SPACING;
        }
        return getSettingsHeight(Feature);
    }

    public static boolean mouseClicked(Feature Feature, double mouseX, double mouseY, int button, int x, int y) {
        if (button < 0 || button > 7) return false;

        int curY = y + SettingRenderer.Feature_SPACING;
        for (Setting<?> setting : Feature.getSettings()) {
            if (!setting.isShow()) continue;
            SettingRenderer<Setting<?>> renderer = (SettingRenderer<Setting<?>>) getRenderer(setting);
            int settingHeight = renderer.getHeight(setting);
            if (mouseX >= x && mouseX <= x + SettingRenderer.WIDTH && mouseY >= curY && mouseY <= curY + settingHeight) {
                draggedSetting = setting;
                dragStartX = mouseX;
                boolean handled = renderer.mouseClicked(setting, mouseX, mouseY, button);
                if (handled) playClickSound();
                return handled;
            }
            curY += settingHeight + SettingRenderer.Feature_SPACING;
        }

        draggedSetting = null;
        return false;
    }

        public static void mouseDragged(double mouseX, double mouseY) {
            if (draggedSetting == null) return;
            SettingRenderer<?> renderer = renderers.get(draggedSetting);
            if (renderer == null) return;

            if (renderer instanceof ColorSettingRenderer colorRenderer) {
                colorRenderer.updateMouseDrag((ColorSetting) draggedSetting, mouseX, mouseY);
            } else if (renderer instanceof IntSettingRenderer intRenderer) {
                intRenderer.updateMouseDrag((IntSetting) draggedSetting, mouseX, mouseY);

            } else if (renderer instanceof DoubleSettingRenderer doubleSetting) {
                doubleSetting.updateMouseDrag((DoubleSetting) draggedSetting, mouseX);
            }else {
                @SuppressWarnings("unchecked")
                SettingRenderer<Setting<?>> generic = (SettingRenderer<Setting<?>>) renderer;
                generic.mouseDragged(draggedSetting, mouseX - dragStartX);
                dragStartX = mouseX;
            }
        }

    public static void mouseReleased(MouseButtonEvent click) {
        if (draggedSetting != null) {
            SettingRenderer<?> renderer = renderers.get(draggedSetting);
            if (renderer != null) {
                @SuppressWarnings("unchecked")
                SettingRenderer<Setting<?>> generic = (SettingRenderer<Setting<?>>) renderer;
                generic.mouseReleased(draggedSetting, click.x(), click.y(), click.button());
            }
        }
        draggedSetting = null;
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y, int height) {
        return mouseX >= x && mouseX <= x + SettingRenderer.WIDTH && mouseY >= y && mouseY <= y + height;
    }

    public static boolean keyPressed(int keyCode) {
        return KeyBindSettingRenderer.keyPressed(keyCode);
    }

    private static void playClickSound() {
        MC.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0f
        ));
    }
}
