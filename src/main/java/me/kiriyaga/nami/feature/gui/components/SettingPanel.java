package me.kiriyaga.nami.feature.gui.components;

import me.kiriyaga.nami.feature.gui.settings.*;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.Setting;
import me.kiriyaga.nami.setting.impl.*;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.kiriyaga.nami.Nami.MC;

public class SettingPanel {
    public static final int INNER_PADDING = 1;
    private static final Map<Class<?>, SettingRenderer<?>> renderers = new HashMap<>();

    private static Setting<?> draggedSetting = null;
    private static double dragStartX = 0;

    static {
        renderers.put(BoolSetting.class, new BoolSettingRenderer());
        renderers.put(ColorSetting.class, new ColorSettingRenderer());
        renderers.put(EnumSetting.class, new EnumSettingRenderer());
        renderers.put(IntSetting.class, new IntSettingRenderer());
        renderers.put(DoubleSetting.class, new DoubleSettingRenderer());
        renderers.put(KeyBindSetting.class, new KeyBindSettingRenderer());
        renderers.put(WhitelistSetting.class, new WhitelistSettingRenderer());
    }

    public static int getSettingsHeight(Module module) {
        List<Setting<?>> settings = module.getSettings();
        int height = 0;
        for (Setting<?> setting : settings) {
            if (!setting.isShow()) continue;

            @SuppressWarnings("unchecked")
            SettingRenderer<Setting<?>> renderer = (SettingRenderer<Setting<?>>) renderers.get(setting.getClass());

            int settingHeight = renderer != null ? renderer.getHeight(setting) : SettingRenderer.HEIGHT;
            height += settingHeight + SettingRenderer.MODULE_SPACING;
        }
        return height + SettingRenderer.MODULE_SPACING;
    }

    public static int renderSettings(DrawContext context, TextRenderer textRenderer, Module module, int x, int y, int mouseX, int mouseY) {
        List<Setting<?>> settings = module.getSettings();
        int curY = y + SettingRenderer.MODULE_SPACING;

        for (Setting<?> setting : settings) {
            if (!setting.isShow()) continue;

            @SuppressWarnings("unchecked")
            SettingRenderer<Setting<?>> renderer = (SettingRenderer<Setting<?>>) renderers.get(setting.getClass());

            if (renderer != null) {
                renderer.render(context, textRenderer, setting, x, curY, mouseX, mouseY);
            }

            int settingHeight = renderer != null ? renderer.getHeight(setting) : SettingRenderer.HEIGHT;
            curY += settingHeight + SettingRenderer.MODULE_SPACING;
        }

        return getSettingsHeight(module);
    }


        @SuppressWarnings("unchecked")
        public static void render(DrawContext context, TextRenderer textRenderer, Setting<?> setting, int x, int y, int mouseX, int mouseY) {
            SettingRenderer<Setting<?>> renderer = (SettingRenderer<Setting<?>>) renderers.get(setting.getClass());
            if (renderer != null) {
                renderer.render(context, textRenderer, setting, x, y, mouseX, mouseY);
            }
        }

    public static boolean mouseClicked(Module module, double mouseX, double mouseY, int button, int x, int y) {
        if (button < 0 || button > 7) return false;

        List<Setting<?>> settings = module.getSettings();
        int curY = y + SettingRenderer.MODULE_SPACING;

        for (Setting<?> setting : settings) {
            if (!setting.isShow()) continue;

            @SuppressWarnings("unchecked")
            SettingRenderer<Setting<?>> renderer = (SettingRenderer<Setting<?>>) renderers.get(setting.getClass());

            int settingHeight = renderer != null ? renderer.getHeight(setting) : SettingRenderer.HEIGHT;

            if (isHovered(mouseX, mouseY, x, curY, settingHeight)) {
                if (renderer != null) {
                    draggedSetting = setting;
                    dragStartX = mouseX;
                    boolean handled = renderer.mouseClicked(setting, mouseX, mouseY, button);
                    if (handled) playClickSound();
                    return handled;
                }
            }

            curY += settingHeight + SettingRenderer.MODULE_SPACING;
        }

        draggedSetting = null;
        return false;
    }

    public static void mouseDragged(double mouseX, double mouseY) {
        if (draggedSetting == null) return;

        SettingRenderer<?> renderer = renderers.get(draggedSetting.getClass());
        if (renderer == null) return;

        if (renderer instanceof ColorSettingRenderer colorRenderer) {
            colorRenderer.updateMouseDrag((ColorSetting) draggedSetting, mouseX, mouseY);
        } else {
            @SuppressWarnings("unchecked")
            SettingRenderer<Setting<?>> generic = (SettingRenderer<Setting<?>>) renderer;
            generic.mouseDragged(draggedSetting, mouseX - dragStartX);
            dragStartX = mouseX;
        }
    }

    public static void mouseReleased() {
        draggedSetting = null;
    }

    public static boolean keyPressed(int keyCode) {
        return KeyBindSettingRenderer.keyPressed(keyCode);
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y, int height) {
        return mouseX >= x && mouseX <= x + SettingRenderer.WIDTH && mouseY >= y && mouseY <= y + height;
    }

    private static void playClickSound() {
        MC.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(
                net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0f
        ));
    }
}
