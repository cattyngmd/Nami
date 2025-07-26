package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.setting.impl.WhitelistSetting;
import net.minecraft.util.Identifier;

import static me.kiriyaga.nami.Nami.*;

public class WhitelistCommand extends Command {
    private final String moduleName;

    public WhitelistCommand(String moduleName) {
        super(
                moduleName.replace(" ", ""),
                new CommandArgument[] {
                        new CommandArgument.StringArg("setting", 1, 64),
                        new CommandArgument.ActionArg("action", "add", "del", "list"),
                        new CommandArgument.StringArg("item", 1, 64) {
                            @Override
                            public boolean isRequired() {
                                return false;
                            }
                        }
                }
        );
        this.moduleName = moduleName;
    }

    @Override
    public void execute(Object[] parsedArgs) {
        String prefix = COMMAND_MANAGER.getExecutor().getPrefix();

        String settingName = ((String) parsedArgs[0]).toLowerCase();
        String action = ((String) parsedArgs[1]).toLowerCase();
        String item = parsedArgs.length > 2 ? (String) parsedArgs[2] : null;
        if (item != null) {
            item = item.toLowerCase().replace("minecraft:", "").trim();
        }

        var module = MODULE_MANAGER.getStorage().getByName(moduleName);
        if (module == null) {
            CHAT_MANAGER.sendPersistent(moduleName,
                    CAT_FORMAT.format("Module {s}{g}" + moduleName + "{s}{reset} not found."));
            return;
        }

        var setting = module.getSettingByName(settingName);
        if (!(setting instanceof WhitelistSetting listSetting)) {
            CHAT_MANAGER.sendPersistent(moduleName,
                    CAT_FORMAT.format("Setting {s}{g}" + settingName + "{s}{reset} not found or not a whitelist setting."));
            return;
        }

        switch (action) {
            case "add" -> {
                if (item == null || item.isEmpty()) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Usage: {s}" + prefix + "{g}" + moduleName + " {g}" + settingName + " add {s}<{g}item{s}>{reset}."));
                    return;
                }
                if (listSetting.addToWhitelist(item)) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Added: {g}minecraft:" + item + "{reset} to {g}" + settingName + "{reset}."));
                } else {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Invalid item id or already added: {g}minecraft:" + item + "{reset}."));
                }
            }
            case "del" -> {
                if (item == null || item.isEmpty()) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Usage: {s}" + prefix + "{g}" + moduleName + " {g}" + settingName + " del {s}<{g}item{s}>{reset}."));
                    return;
                }
                if (listSetting.removeFromWhitelist(item)) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Removed: {g}minecraft:" + item + "{reset} from {g}" + settingName + "{reset}."));
                } else {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Invalid or not in list: {g}minecraft:" + item + "{reset}."));
                }
            }
            case "list" -> {
                if (listSetting.getWhitelist().isEmpty()) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("List {g}" + settingName + "{reset} is empty."));
                    return;
                }
                StringBuilder builder = new StringBuilder();
                builder.append("List {g}").append(settingName).append("{reset} items: ");

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