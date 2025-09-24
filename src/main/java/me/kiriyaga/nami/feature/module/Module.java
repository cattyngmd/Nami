package me.kiriyaga.nami.feature.module;

import me.kiriyaga.nami.feature.setting.Setting;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.KeyBindSetting;
import net.minecraft.text.Text;

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
    private String displayInfo = "";

    protected final List<Setting<?>> settings = new ArrayList<>();
    protected final KeyBindSetting keyBind;

    public Module(String name, String description, ModuleCategory category, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.category = category;

        this.keyBind = new KeyBindSetting("Bind", KeyBindSetting.KEY_NONE);
        this.drawn = new BoolSetting("Drawn", false);
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
        if (MC.world == null) return;
        if (this.enabled == state) return;

        this.enabled = state;

        if (enabled) {
            EVENT_MANAGER.register(this);
            onEnable();

            Text message = CAT_FORMAT.format("{s}[{g}+{s}] {reset}" + name);
            CHAT_MANAGER.sendTransient(message, false);

        } else {
            EVENT_MANAGER.unregister(this);
            onDisable();

            Text message = CAT_FORMAT.format("{namiDarkRed}[{namiRed}-{namiDarkRed}] {reset}" + name);
            CHAT_MANAGER.sendTransient(message, false);
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

    public void setDisplayInfo(String info) {
        this.displayInfo = info;
    }

    public String getDisplayInfo() {
        if (displayInfo != null && !displayInfo.isEmpty()) {
            return  displayInfo;
        }
        return null;
    }

    protected void onEnable() {}

    protected void onDisable() {}
}
