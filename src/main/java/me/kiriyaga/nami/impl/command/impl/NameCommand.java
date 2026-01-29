package me.kiriyaga.nami.impl.command.impl;

import me.kiriyaga.nami.impl.command.Command;
import me.kiriyaga.nami.impl.command.CommandArgument;
import me.kiriyaga.nami.impl.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class NameCommand extends Command {

    public NameCommand() {
        super(
                "name",
                new CommandArgument[] {
                        new CommandArgument.StringArg("name", 1, 24)
                },
                "n", "nam", "mne", "nome", "brand", "changename"
        );
    }

    @Override
    public void execute(Object[] args) {
        String newName = (String) args[0];

        DISPLAY_NAME = newName;
        CONFIG_SERVICE.saveName(newName);

        CHAT_SERVICE.sendPersistent(NameCommand.class.getName(),
                CAT_FORMAT.format("Name set to: {g}" + newName + "{reset}."));
    }
}