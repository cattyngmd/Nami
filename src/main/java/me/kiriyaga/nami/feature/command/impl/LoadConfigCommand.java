package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class LoadConfigCommand extends Command {

    public LoadConfigCommand() {
        super("loadconfig", "Load a config by name", "loadcfg", "lcfg");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            String prefix = COMMAND_MANAGER.getExecutor().getPrefix();
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Usage: {global}" + prefix + "loadconfig <configName>{reset}."));
            return;
        }
        String configName = args[0];

        try {
            CONFIG_MANAGER.loadConfig(configName);
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Config {global}" + configName + "{reset} has been loaded."));
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Failed to load config {global}" + configName + "{reset}: {global}" + e + "{reset}."));
        }
    }
}
