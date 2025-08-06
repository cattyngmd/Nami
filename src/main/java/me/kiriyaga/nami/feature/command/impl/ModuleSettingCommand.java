package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.Setting;
import me.kiriyaga.nami.setting.impl.*;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class ModuleSettingCommand extends Command {

    public ModuleSettingCommand() {
        super("set", new CommandArgument[] {
                new CommandArgument.StringArg("module", 1, 64),
                new CommandArgument.StringArg("setting", 1, 64),
                new CommandArgument.StringArg("value", 1, 256) {
                    @Override
                    public boolean isRequired() {
                        return false;
                    }
                }
        });
    }

    @Override
    public void execute(Object[] parsedArgs) {
        String prefix = COMMAND_MANAGER.getExecutor().getPrefix();

        String moduleNameRaw = ((String) parsedArgs[0]);
        String settingNameRaw = ((String) parsedArgs[1]);
        String valueRaw = parsedArgs.length > 2 ? (String) parsedArgs[2] : null;

        String moduleName = moduleNameRaw.replace(" ", "").toLowerCase();
        String settingName = settingNameRaw.replace(" ", "").toLowerCase();

        Module module = null;
        for (Module m : MODULE_MANAGER.getStorage().getAll()) {
            if (m.getName().replace(" ", "").toLowerCase().equals(moduleName)) {
                module = m;
                break;
            }
        }
        if (module == null) {
            CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Module {g}" + moduleNameRaw + "{reset} not found."));
            return;
        }

        Setting<?> setting = null;
        for (Setting<?> s : module.getSettings()) {
            if (s.getName().replace(" ", "").toLowerCase().equals(settingName)) {
                setting = s;
                break;
            }
        }

        if (setting == null) {
            CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Setting {g}" + settingNameRaw + "{reset} not found in module {g}" + module.getName() + "{reset}."));
            return;
        }

        if (setting instanceof BoolSetting boolSetting) {
            if (valueRaw == null) {
                boolSetting.set(!boolSetting.get());
                CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format(
                        "Bool setting {g}" + setting.getName() + "{reset} toggled to {g}" + boolSetting.get() + "{reset}."));
            } else {
                if (valueRaw.equalsIgnoreCase("true") || valueRaw.equalsIgnoreCase("on")) {
                    boolSetting.set(true);
                } else if (valueRaw.equalsIgnoreCase("false") || valueRaw.equalsIgnoreCase("off")) {
                    boolSetting.set(false);
                } else {
                    CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Invalid bool value {g}" + valueRaw + "{reset}. Use true/false."));
                    return;
                }
                CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format(
                        "Bool setting {g}" + setting.getName() + "{reset} set to {g}" + boolSetting.get() + "{reset}."));
            }
        } else if (setting instanceof IntSetting intSetting) {
            if (valueRaw == null) {
                CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Please specify an integer value for setting {g}" + setting.getName() + "{reset}."));
                return;
            }
            try {
                int val = Integer.parseInt(valueRaw);
                intSetting.set(val);
                CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Int setting {g}" + setting.getName() + "{reset} set to {g}" + val + "{reset}."));
            } catch (NumberFormatException e) {
                CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Invalid integer value {g}" + valueRaw + "{reset}."));
            }
        } else if (setting instanceof DoubleSetting doubleSetting) {
            if (valueRaw == null) {
                CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Please specify a number value for setting {g}" + setting.getName() + "{reset}."));
                return;
            }
            try {
                double val = Double.parseDouble(valueRaw);
                doubleSetting.set(val);
                CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Double setting {g}" + setting.getName() + "{reset} set to {g}" + val + "{reset}."));
            } catch (NumberFormatException e) {
                CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Invalid number value {g}" + valueRaw + "{reset}."));
            }
        } else if (setting instanceof WhitelistSetting whitelistSetting) {
            if (valueRaw == null) {
                CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Whitelist settings require an action: add/del/list."));
                return;
            }
            String[] parts = valueRaw.split(" ", 2);
            String action = parts[0].toLowerCase();
            String item = parts.length > 1 ? parts[1].toLowerCase().trim() : null;

            switch (action) {
                case "add" -> {
                    if (item == null || item.isEmpty()) {
                        CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Usage: {g}" + prefix + " set " + module.getName() + " " + setting.getName() + " add <item>{reset}."));
                        return;
                    }
                    if (whitelistSetting.addToWhitelist(item)) {
                        CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Added: {g}minecraft:" + item + "{reset} to {g}" + setting.getName() + "{reset}."));
                    } else {
                        CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Invalid item id or already added: {g}minecraft:" + item + "{reset}."));
                    }
                }
                case "del" -> {
                    if (item == null || item.isEmpty()) {
                        CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Usage: {g}" + prefix + " set " + module.getName() + " " + setting.getName() + " del <item>{reset}."));
                        return;
                    }
                    if (whitelistSetting.removeFromWhitelist(item)) {
                        CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Removed: {g}minecraft:" + item + "{reset} from {g}" + setting.getName() + "{reset}."));
                    } else {
                        CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Invalid or not in list: {g}minecraft:" + item + "{reset}."));
                    }
                }
                case "list" -> {
                    if (whitelistSetting.getWhitelist().isEmpty()) {
                        CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("List {g}" + setting.getName() + "{reset} is empty."));
                        return;
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append("List {g}").append(setting.getName()).append("{reset} items: ");

                    int i = 0;
                    int size = whitelistSetting.getWhitelist().size();
                    for (var id : whitelistSetting.getWhitelist()) {
                        builder.append("{g}").append(id.toString()).append("{reset}");
                        if (i < size - 1) builder.append("{g}, {reset}");
                        i++;
                    }
                    builder.append(".");
                    CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format(builder.toString()));
                }
                default -> CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Unknown action: {g}" + action + "{reset}. Use add/del/list."));
            }
        } else if (setting instanceof KeyBindSetting keyBindSetting) {
            if (valueRaw == null) {
                CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Please specify a keybind value for setting {g}" + setting.getName() + "{reset}."));
                return;
            }
            try {
                int keyCode = Integer.parseInt(valueRaw);
                keyBindSetting.set(keyCode);
                CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("KeyBind setting {g}" + setting.getName() + "{reset} set to key code {g}" + keyCode + "{reset}."));
            } catch (NumberFormatException e) {
                CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Invalid keybind value {g}" + valueRaw + "{reset}. Must be a key code integer."));
            }
        } else {
            CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Setting type of {g}" + setting.getName() + "{reset} is not supported by this command."));
        }
    }
}
