package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.COMMAND_MANAGER;

@RegisterCommand
public class ChangePrefixCommand extends Command {

    public ChangePrefixCommand() {
        super("prefix", "Changes your command prefix. Usage: .prefix <char>", "changeprefix", "сз", "зкуашч");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), "Usage: .prefix §7<char>");
            return;
        }

        String input = args[0].trim();

        if (input.isEmpty()) {
            CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), "Usage: .prefix §7<char>");
            return;
        }

        if (input.length() > 1) {
            CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), "Usage: .prefix v<char>");
            return;
        }

        COMMAND_MANAGER.setPrefix(input);
        CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), "Prefix changed to: §7" + input);
    }
}
