package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.CONFIG_MANAGER;

public class LoadCommand extends Command {

    public LoadCommand() {
        super("load", "Force config load", "l", "laod", "lad", "lod", "дщфв");
    }

    @Override
    public void execute(String[] args) {
        try {
            CONFIG_MANAGER.load();
            CHAT_MANAGER.sendPersistent(LoadCommand.class.getName(), "Config has been loaded.");
        } catch (Exception e){
            CHAT_MANAGER.sendPersistent(LoadCommand.class.getName(), "Config has not been loaded: §7" + e);
        }
    }
}
