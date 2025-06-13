package me.kiriyaga.essentials.feature.command.impl;

import me.kiriyaga.essentials.feature.command.Command;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.COMMAND_MANAGER;

public class ChangePrefixCommand extends Command {

    public ChangePrefixCommand() {
        super("changeprefix", "Changes your command prefix. Usage: .changeprefix <char>", "cp", "сз", " cp");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), "Usage: .changeprefix <char>");
            return;
        }

        String input = args[0].trim();

        if (input.isEmpty()) {
            CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), "Prefix cannot be empty.");
            return;
        }

        if (input.length() > 1) {
            CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), "Prefix must be a single character.");
            return;
        }

        COMMAND_MANAGER.setPrefix(input);
        CHAT_MANAGER.sendPersistent(ChangePrefixCommand.class.getName(), "Prefix changed to: §e" + input);
    }
}
