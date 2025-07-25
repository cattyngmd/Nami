package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.feature.command.CommandArgument;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class SaveConfigCommand extends Command {

    public SaveConfigCommand() {
        super("saveconfig",
                new CommandArgument[] {
                        new CommandArgument.StringArg("configName", 1, 32)
                },
                "savecfg", "scfg");
    }

    @Override
    public void execute(Object[] args) {
        if (args.length < 1) {
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Usage: {s}" + COMMAND_MANAGER.getExecutor().getPrefix() + "{g}saveconfig {s}<{g}configName{s}>{reset}."));
            return;
        }

        String configName = args[0].toString();

        try {
            CONFIG_MANAGER.saveConfig(configName);
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Config {g}" + configName + "{reset} has been saved."));
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Failed to save config {g}" + configName + "{reset}: {g}" + e + "{reset}."));
        }
    }
}
