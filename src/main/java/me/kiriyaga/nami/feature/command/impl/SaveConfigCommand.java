package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.CONFIG_MANAGER;

@RegisterCommand
public class SaveConfigCommand extends Command {

    public SaveConfigCommand() {
        super("saveconfig", "Save a config by name", "savecfg", "scfg");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CHAT_MANAGER.sendPersistent(getClass().getName(), "Usage: saveconfig §7<configName>");
            return;
        }
        String configName = args[0];

        try {
            CONFIG_MANAGER.saveConfig(configName);
            CHAT_MANAGER.sendPersistent(getClass().getName(), "Config §7\"" + configName + "\" §fhas been saved.");
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(getClass().getName(), "Failed to save config §7\"" + configName + "\": §7" + e);
        }
    }
}
