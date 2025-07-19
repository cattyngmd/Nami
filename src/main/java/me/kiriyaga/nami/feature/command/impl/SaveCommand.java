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
            CONFIG_MANAGER.save();
            CHAT_MANAGER.sendPersistent(SaveCommand.class.getName(), "Config has been saved.");
        } catch (Exception e){
            CHAT_MANAGER.sendPersistent(SaveCommand.class.getName(), "Config has not been saved: §7" + e);
        }
    }
}
