package me.kiriyaga.nami.feature.module;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.setting.Setting;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.KeyBindSetting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import static me.kiriyaga.nami.Nami.*;

public abstract class Module {

    protected final String name;
    protected final String description;
    protected final String[] aliases;
    protected final ModuleCategory category;

    private BoolSetting drawn;
    private boolean enabled = false;
    private final List<Component> displayInfo = new ArrayList<>();

    protected final List<Setting<?>> settings = new ArrayList<>();
    protected final KeyBindSetting keyBind;
    private boolean expanded;

    public Module(String name, String description, ModuleCategory category, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.category = category;
        expanded = false;

        this.keyBind = new KeyBindSetting("Bind", KeyBindSetting.KEY_NONE);
        this.drawn = new BoolSetting("Drawn", true);
        this.drawn.setShow(false);
        addSetting(keyBind);
        addSetting(drawn);
    }


    public void toggle() {
        setEnabled(!enabled);
    }

    public void setDrawn(boolean state){
        this.drawn.set(state);
    }

    public void setEnabled(boolean state) {
        if (this.enabled == state) return;

        this.enabled = state;

        if (enabled) {
            EVENT_MANAGER.register(this);
            onEnable();

        } else {
            EVENT_MANAGER.unregister(this);
            onDisable();

        }

        if (MC.level != null && MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).moduleChatFeedback.get()) {
            Component message = CAT_FORMAT.format("{g}"+name + "{reset} toggled" + (enabled ? " {green}on" : " {red}off") + "{reset}.");
            CHAT_MANAGER.sendPersistent(name, message);
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

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isDrawn() {
        return drawn.get();
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
        setting.setParentModule(this);
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

    public Setting<?> getSettingByName(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase();
        for (Setting<?> setting : settings) {
            String sname = setting.getName();
            if (sname == null) continue;
            if (sname.toLowerCase().equals(lower)) {
                return setting;
            }
        }
        String compact = lower.replaceAll("\\s", "");
        for (Setting<?> setting : settings) {
            String sname = setting.getName();
            if (sname == null) continue;
            if (sname.toLowerCase().replaceAll("\\s", "").equals(compact)) {
                return setting;
            }
        }
        return null;
    }

    public void addDisplayInfo(Component info) {
        if (info == null) return;
        if (info.getString().isEmpty()) return;

        displayInfo.add(info);
    }

    public void addDisplayInfo(String info) {
        if (info == null || info.isEmpty()) return;
        displayInfo.add(Component.literal(info));
    }

    public void clearDisplayInfo() {
        displayInfo.clear();
    }

    public List<Component> getDisplayInfo() {
        if (displayInfo.isEmpty()) return null;
        return displayInfo;
    }

    protected void onEnable() {}

    protected void onDisable() {}
}
