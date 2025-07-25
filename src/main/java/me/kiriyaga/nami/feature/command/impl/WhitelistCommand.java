package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.setting.impl.WhitelistSetting;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;

import static me.kiriyaga.nami.Nami.*;

public class WhitelistCommand extends Command {
    private final String moduleName;

    public WhitelistCommand(String moduleName) {
        super(moduleName.replace(" ", ""), "Manage list settings for module " + moduleName);
        this.moduleName = moduleName;
    }

    @Override
    public void execute(String[] args) {
        String prefix = COMMAND_MANAGER.getExecutor().getPrefix();

        if (args.length < 1) {
            CHAT_MANAGER.sendPersistent(moduleName,
                    CAT_FORMAT.format("Usage: {s}" + prefix + "{g}" + moduleName + "{s} <{g}setting{s}> <{g}add/del/list{s}> <{g}item{s}>{reset}."));
            return;
        }

        String settingName = args[0].toLowerCase();

        var module = MODULE_MANAGER.getStorage().getByName(moduleName);
        if (module == null) {
            CHAT_MANAGER.sendPersistent(moduleName,
                    CAT_FORMAT.format("Module {s}'{g}" + moduleName + "{s}'{reset} not found."));
            return;
        }

        var setting = module.getSettingByName(settingName);
        if (!(setting instanceof WhitelistSetting listSetting)) {
            CHAT_MANAGER.sendPersistent(moduleName,
                    CAT_FORMAT.format("Setting {s}'{g}" + settingName + "{s}'{reset} not found or not a whitelist setting."));
            return;
        }

        if (args.length < 2) {
            CHAT_MANAGER.sendPersistent(moduleName,
                    CAT_FORMAT.format("Usage: {s}" + prefix + "{g}" + moduleName + " {g}" + settingName +
                            " {s}<{g}add/del/list{s}> [{g}item{s}]{reset}."));
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "add" -> {
                if (args.length < 3) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Usage: {s}" + prefix + "{g}" + moduleName + " {g}" + settingName +
                                    " add {s}<{g}item{s}>{reset}."));
                    return;
                }
                String addItem = args[2].toLowerCase().replace("minecraft:", "");
                if (listSetting.addToWhitelist(addItem)) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Added: {g}minecraft:" + addItem + "{reset} to {g}" + settingName + "{reset}."));
                } else {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Invalid item id or already added: {g}minecraft:" + addItem + "{reset}."));
                }
            }

            case "del" -> {
                if (args.length < 3) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Usage: {s}" + prefix + "{g}" + moduleName + " {g}" + settingName +
                                    " del {s}<{g}item{s}>{reset}."));
                    return;
                }
                String delItem = args[2].toLowerCase().replace("minecraft:", "");
                if (listSetting.removeFromWhitelist(delItem)) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Removed: {g}minecraft:" + delItem + "{reset} from {g}" + settingName + "{reset}."));
                } else {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Invalid or not in list: {g}minecraft:" + delItem + "{reset}."));
                }
            }

            case "list" -> {
                if (listSetting.getWhitelist().isEmpty()) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("{g}" + settingName + "{reset} is empty."));
                    return;
                }

                StringBuilder builder = new StringBuilder();
                builder.append("{g}").append(settingName).append("{reset} items: ");

                int i = 0;
                int size = listSetting.getWhitelist().size();
                for (Identifier id : listSetting.getWhitelist()) {
                    builder.append("{g}").append(id.toString()).append("{reset}");
                    if (i < size - 1) builder.append("{s}, {reset}");
                    i++;
                }

                CHAT_MANAGER.sendPersistent(moduleName, CAT_FORMAT.format(builder.toString()));
            }

            default -> CHAT_MANAGER.sendPersistent(moduleName,
                    CAT_FORMAT.format("Unknown action: {g}" + action + "{reset}. Use {g}add/del/list{reset}."));
        }
    }
}
