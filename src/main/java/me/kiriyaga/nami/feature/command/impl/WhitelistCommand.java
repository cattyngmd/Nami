package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.setting.impl.WhitelistSetting;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.Nami.CAT_FORMAT;

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
                    CAT_FORMAT.format("Usage: ")
                            .append(CAT_FORMAT.format("{global}" + prefix))
                            .append(CAT_FORMAT.format(moduleName + " {global}<settingName> <add/del/list> <item>{reset}.")));
            return;
        }

        String settingName = args[0].toLowerCase();

        var module = MODULE_MANAGER.getStorage().getByName(moduleName);
        if (module == null) {
            CHAT_MANAGER.sendPersistent(moduleName,
                    CAT_FORMAT.format("Module {global}")
                            .append(CAT_FORMAT.format(moduleName + "{reset} not found.")));
            return;
        }

        var setting = module.getSettingByName(settingName);
        if (!(setting instanceof WhitelistSetting listSetting)) {
            CHAT_MANAGER.sendPersistent(moduleName,
                    CAT_FORMAT.format("Setting {global}")
                            .append(CAT_FORMAT.format(settingName + "{reset} not found or not a whitelist setting.")));
            return;
        }

        if (args.length < 2) {
            CHAT_MANAGER.sendPersistent(moduleName,
                    CAT_FORMAT.format("Usage: ")
                            .append(CAT_FORMAT.format("{global}" + prefix))
                            .append(CAT_FORMAT.format(moduleName + " " + settingName + " <add/del/list> [item]{reset}.")));
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "add" -> {
                if (args.length < 3) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Usage: ")
                                    .append(CAT_FORMAT.format("{global}" + prefix))
                                    .append(CAT_FORMAT.format(moduleName + " " + settingName + " add <item>{reset.}")));
                    return;
                }
                String addItem = args[2].toLowerCase().replace("minecraft:", "");
                if (listSetting.addToWhitelist(addItem)) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Added: {global}")
                                    .append(CAT_FORMAT.format("minecraft:" + addItem + " {reset}to{global} " + settingName + "{reset}.")));
                } else {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Invalid item id or already added: {global}")
                                    .append(CAT_FORMAT.format("{global}" + prefix))
                                    .append(CAT_FORMAT.format("minecraft:" + addItem + "{reset}.")));
                }
            }
            case "del" -> {
                if (args.length < 3) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Usage: ")
                                    .append(CAT_FORMAT.format("{global}" + prefix))
                                    .append(CAT_FORMAT.format(moduleName + " " + settingName + " del <item>{reset}.")));
                    return;
                }
                String delItem = args[2].toLowerCase().replace("minecraft:", "");
                if (listSetting.removeFromWhitelist(delItem)) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Removed: {global}")
                                    .append(CAT_FORMAT.format("minecraft:" + delItem + " {reset}from{global} " + settingName+"{reset}.")));
                } else {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("Invalid or not in list: {global}")
                                    .append(CAT_FORMAT.format("minecraft:" + delItem + "{reset}.")));
                }
            }
            case "list" -> {
                if (listSetting.getWhitelist().isEmpty()) {
                    CHAT_MANAGER.sendPersistent(moduleName,
                            CAT_FORMAT.format("{global}"+settingName + " {reset}is empty."));
                    return;
                }
                MutableText builder = CAT_FORMAT.format("{global}"+settingName + "{reset} items:{global} ");
                int i = 0;
                int size = listSetting.getWhitelist().size();
                for (Identifier id : listSetting.getWhitelist()) {
                    builder.append(CAT_FORMAT.format("{global}" + prefix)).append(CAT_FORMAT.format(id.toString()));
                    if (i < size - 1) builder.append(CAT_FORMAT.format("{reset}, {global}"));
                    i++;
                }
                CHAT_MANAGER.sendPersistent(moduleName, builder);
            }
            default -> CHAT_MANAGER.sendPersistent(moduleName,
                    CAT_FORMAT.format("Unknown action: ")
                            .append(CAT_FORMAT.format("{global}" + prefix))
                            .append(CAT_FORMAT.format(action + ". {reset}Use "))
                            .append(CAT_FORMAT.format("{global}" + prefix))
                            .append(CAT_FORMAT.format("add/del/list")));
        }
    }
}