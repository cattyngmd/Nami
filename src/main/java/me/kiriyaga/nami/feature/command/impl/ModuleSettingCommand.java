package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.setting.Setting;
import me.kiriyaga.nami.feature.setting.impl.*;

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
                } else if (valueRaw.equalsIgnoreCase("toggle")) {
                    boolSetting.set(!boolSetting.get());
                } else {
                    CHAT_MANAGER.sendPersistent("set", CAT_FORMAT.format("Invalid bool value {g}" + valueRaw + "{reset}. Use {g}true/false{reset}."));
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
