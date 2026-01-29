package me.kiriyaga.nami.impl.command.impl;

import me.kiriyaga.nami.impl.command.Command;
import me.kiriyaga.nami.impl.command.CommandArgument;
import me.kiriyaga.nami.impl.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class LoadCommand extends Command {

    public LoadCommand() {
        super("load",
                new CommandArgument[] {},
                "l", "laod", "lad", "lod");
    }

    @Override
    public void execute(Object[] args) {
        try {
            CONFIG_SERVICE.loadFeatures();
            CHAT_SERVICE.sendPersistent(LoadCommand.class.getName(),
                    CAT_FORMAT.format("Config has been loaded."));
        } catch (Exception e) {
            CHAT_SERVICE.sendPersistent(LoadCommand.class.getName(),
                    CAT_FORMAT.format("Config has not been loaded: {g}" + e.getMessage() + "{reset}."));
        }
    }
}
