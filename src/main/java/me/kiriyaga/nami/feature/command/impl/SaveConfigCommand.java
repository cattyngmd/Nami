package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class SaveConfigCommand extends Command {

    public SaveConfigCommand() {
        super("saveconfig", "Save a config by name", "savecfg", "scfg");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Usage: {global}" + COMMAND_MANAGER.getExecutor().getPrefix() + "saveconfig <configName>{reset}."));
            return;
        }

        String configName = args[0];

        try {
            CONFIG_MANAGER.saveConfig(configName);
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Config {global}" + configName + "{reset} has been saved."));
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Failed to save config {global}" + configName + "{reset}: {global}" + e + "{reset}."));
        }
    }
}
