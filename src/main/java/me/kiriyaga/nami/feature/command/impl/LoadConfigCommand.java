package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.CONFIG_MANAGER;

@RegisterCommand
public class LoadConfigCommand extends Command {

    public LoadConfigCommand() {
        super("loadconfig", "Load a config by name", "loadcfg", "lcfg");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CHAT_MANAGER.sendPersistent(getClass().getName(), "Usage: .loadconfig §7<configName>");
            return;
        }
        String configName = args[0];

        try {
            CONFIG_MANAGER.loadConfig(configName);
            CHAT_MANAGER.sendPersistent(getClass().getName(), "Config §7\"" + configName + "\" §fhas been loaded.");
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(getClass().getName(), "Failed to load config §7\"" + configName + "\": §7" + e);
        }
    }
}
