package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.annotation.RegisterCommand;

@RegisterCommand
public class SaveCommand extends Command {

    public SaveCommand() {
        super("save",
                new CommandArgument[0], "s", "save", "seva", "sv");
    }

    @Override
    public void execute(Object[] args) {
        try {
            CONFIG_SERVICE.saveFeatures();
            CHAT_SERVICE.sendPersistent(SaveCommand.class.getName(),
                    CAT_FORMAT.format("Config has been saved."));
        } catch (Exception e) {
            CHAT_SERVICE.sendPersistent(SaveCommand.class.getName(),
                    CAT_FORMAT.format("Config has not been saved: {g}" + e + "{reset}."));
        }
    }
}
