package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class ChangePrefixCommand extends Command {

    public ChangePrefixCommand() {
        super("prefix", "Changes your command prefix. Usage: .prefix <char>", "changeprefix", "сз", "зкуашч");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            Text message = CAT_FORMAT.format("Usage: {s}" + COMMAND_MANAGER.getExecutor().getPrefix() + "{g}prefix {s}<{g}char{s}>{reset}.");
            CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), message);
            return;
        }

        String input = args[0].trim();

        if (input.isEmpty()) {
            Text message = CAT_FORMAT.format("Usage: {s}" + COMMAND_MANAGER.getExecutor().getPrefix() + "{g}prefix {s}<{g}char{s}>{reset}.");
            CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), message);
            return;
        }

        if (input.length() > 1) {
            Text message = CAT_FORMAT.format("Usage: {s}" + COMMAND_MANAGER.getExecutor().getPrefix() + "{g}prefix {s}<{g}char{s}>{reset}.");
            CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), message);
            return;
        }

        COMMAND_MANAGER.getExecutor().setPrefix(input);
        CONFIG_MANAGER.savePrefix(input);

        Text message = CAT_FORMAT.format("Prefix changed to: {g}" + input + "{reset}.");
        CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), message);
    }
}
