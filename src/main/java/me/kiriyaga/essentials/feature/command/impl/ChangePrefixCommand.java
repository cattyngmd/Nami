package me.kiriyaga.essentials.feature.command.impl;

import me.kiriyaga.essentials.feature.command.Command;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.COMMAND_MANAGER;

public class ChangePrefixCommand extends Command {

    public ChangePrefixCommand() {
        super("prefix", "Changes your command prefix. Usage: .prefix <char>", "changeprefix", "сз", "зкуашч");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), "Usage: .prefix <char>");
            return;
        }

        String input = args[0].trim();

        if (input.isEmpty()) {
            CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), "Usage: .prefix <char>");
            return;
        }

        if (input.length() > 1) {
            CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), "Usage: .prefix <char>");
            return;
        }

        COMMAND_MANAGER.setPrefix(input);
        CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), "Prefix changed to: §7" + input);
    }
}
