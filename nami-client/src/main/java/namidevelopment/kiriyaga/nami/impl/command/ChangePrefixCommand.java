package namidevelopment.kiriyaga.nami.impl.command.impl;

import namidevelopment.kiriyaga.nami.impl.command.Command;
import namidevelopment.kiriyaga.nami.impl.command.CommandArgument;
import namidevelopment.kiriyaga.nami.impl.command.RegisterCommand;
import net.minecraft.network.chat.Component;

@RegisterCommand
public class ChangePrefixCommand extends Command {

    public ChangePrefixCommand() {
        super(
                "prefix",
                new CommandArgument[] {
                        new CommandArgument.StringArg("char", 1, 1)
                },
                "changeprefix"
        );
    }

    @Override
    public void execute(Object[] args) {
        String input = ((String) args[0]).trim();

        COMMAND_SERVICE.getExecutor().setPrefix(input);
        CONFIG_SERVICE.savePrefix(input);

        Component message = CAT_FORMAT.format("Prefix changed to: {g}" + input + "{reset}.");
        CHAT_SERVICE.sendPersistent(ChangePrefixCommand.class.getName(), message);
    }
}
