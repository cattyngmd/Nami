package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.annotation.RegisterCommand;

import static namidevelopment.kiriyaga.api.NamiApi.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;
import static namidevelopment.kiriyaga.nami.Nami.DISPLAY_NAME;
import static namidevelopment.kiriyaga.nami.Nami.MC;

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