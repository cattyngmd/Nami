package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.CONFIG_MANAGER;
import static me.kiriyaga.nami.Nami.CAT_FORMAT;

@RegisterCommand
public class LoadCommand extends Command {

    public LoadCommand() {
        super("load", "Force config load", "l", "laod", "lad", "lod", "дщфв");
    }

    @Override
    public void execute(String[] args) {
        try {
            CONFIG_MANAGER.loadModules();
            CHAT_MANAGER.sendPersistent(LoadCommand.class.getName(),
                    CAT_FORMAT.format("Config has been loaded."));
        } catch (Exception e){
            CHAT_MANAGER.sendPersistent(LoadCommand.class.getName(),
                    CAT_FORMAT.format("Config has not been loaded: {g}" + e + "{reset}."));
        }
    }
}