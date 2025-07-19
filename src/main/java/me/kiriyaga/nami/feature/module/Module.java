package me.kiriyaga.nami.feature.module;

import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.setting.Setting;
import me.kiriyaga.nami.setting.impl.KeyBindSetting;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static me.kiriyaga.nami.Nami.*;

public abstract class Module {

    protected final String name;
    protected final String description;
    protected final String[] aliases;
    protected final ModuleCategory category;

    private boolean enabled = false;
    private String displayInfo = "";

    protected final List<Setting<?>> settings = new ArrayList<>();
    protected final KeyBindSetting keyBind;

    public Module(String name, String description, ModuleCategory category, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.category = category;

        this.keyBind = new KeyBindSetting("bind", KeyBindSetting.KEY_NONE);
        this.settings.add(keyBind);
    }


    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean state) {
        if (this.enabled == state) return;

        this.enabled = state;

        ColorModule cm = MODULE_MANAGER.getModule(ColorModule.class);

        if (enabled) {
            EVENT_MANAGER.register(this);
            onEnable();

            if (cm != null) {
                Color bracketColor = cm.getStyledSecondColor();
                Color plusColor = cm.getStyledGlobalColor();

                Style bracketStyle = Style.EMPTY.withColor(bracketColor.getRGB());
                Style plusStyle = Style.EMPTY.withColor(plusColor.getRGB());

                Text message = Text.empty()
                        .append(Text.literal("[").setStyle(bracketStyle))
                        .append(Text.literal("+").setStyle(plusStyle))
                        .append(Text.literal("] ").setStyle(bracketStyle))
                        .append(Text.literal(name));

                CHAT_MANAGER.sendTransient(message, false);
            }
        } else {
            EVENT_MANAGER.unregister(this);
            onDisable();

            if (cm != null) {
                Color baseRed = new Color(220, 0, 0);
                Color bracketRed = cm.applyDarkness(baseRed, 0.5);

                Style bracketStyle = Style.EMPTY.withColor(bracketRed.getRGB());
                Style minusStyle = Style.EMPTY.withColor(baseRed.getRGB());

                Text message = Text.empty()
                        .append(Text.literal("[").setStyle(bracketStyle))
                        .append(Text.literal("-").setStyle(minusStyle))
                        .append(Text.literal("] ").setStyle(bracketStyle))
                        .append(Text.literal(name));

                CHAT_MANAGER.sendTransient(message, false);
            }
        }
    }


    public ModuleCategory getCategory() {
        return category;
    }

    public KeyBindSetting getKeyBind() {
        return keyBind;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAliases() {
        return aliases;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public <T extends Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public boolean matches(String input) {
        String lower = input.toLowerCase();
        if (lower.equals(name.toLowerCase())) return true;
        for (String alias : aliases) {
            if (lower.equals(alias.toLowerCase())) return true;
        }
        return false;
    }

    public String getDisplayName() {
        if (displayInfo != null && !displayInfo.isEmpty()) {
            return name + "[" + displayInfo + "]";
        }
        return name;
    }

    public Setting<?> getSettingByName(String name) {
        String lower = name.toLowerCase();
        for (Setting<?> setting : settings) {
            if (setting.getName().toLowerCase().equals(lower)) {
                return setting;
            }
        }
        return null;
    }

    public void setDisplayInfo(String info) {
        this.displayInfo = info;
    }

    public String getDisplayInfo() {
        return displayInfo;
    }

    protected void onEnable() {}

    protected void onDisable() {}
}
