package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class SaveCommand extends Command {

    public SaveCommand() {
        super("save", "Force config save.", "s", "save", "seva", "sv", "ыфму");
    }

    @Override
    public void execute(String[] args) {
        try {
            CONFIG_MANAGER.saveModules();
            CHAT_MANAGER.sendPersistent(SaveCommand.class.getName(),
                    CAT_FORMAT.format("Config has been saved."));
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(SaveCommand.class.getName(),
                    CAT_FORMAT.format("Config has not been saved: {global}" + e + "{reset}."));
        }
    }
}