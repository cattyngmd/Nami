package namidevelopment.kiriyaga.nami.impl.command.impl;

import namidevelopment.kiriyaga.nami.impl.command.Command;
import namidevelopment.kiriyaga.nami.impl.command.CommandArgument;
import namidevelopment.kiriyaga.nami.impl.command.RegisterCommand;

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
