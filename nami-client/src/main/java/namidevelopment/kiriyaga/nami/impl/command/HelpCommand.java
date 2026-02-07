package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.annotation.RegisterCommand;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.stream.Collectors;

import static namidevelopment.kiriyaga.api.NamiApi.*;

@RegisterCommand
public class HelpCommand extends Command {

    public HelpCommand() {
        super("help",
                new CommandArgument[] {},
                "h", "?", "hlp", "halp", "hilp", "heil", "commands", "command");
    }

    @Override
    public void execute(Object[] args) {
        List<Command> cmds = COMMAND_SERVICE.getStorage().getCommands();

        if (cmds.isEmpty()) {
            CHAT_SERVICE.sendPersistent(HelpCommand.class.getName(),
                    CAT_FORMAT.format("No commands registered."));
            return;
        }

        // TODO: when addon impl, rewrite theese to dynamic
        String displayText = cmds.stream()
                .filter(c -> c.getName() != null)
                .map(this::getDisplay)
                .collect(Collectors.joining(", "));

        MutableComponent message = CAT_FORMAT.format("{gray}Available commands: " + displayText + "{white}.");
        CHAT_SERVICE.sendPersistent(HelpCommand.class.getName(), message);
    }

    private String getDisplay(Command command) {
        String display = command.getName().replace(" ", "");
        return "{global}" + display + "{gray}";
    }
}
