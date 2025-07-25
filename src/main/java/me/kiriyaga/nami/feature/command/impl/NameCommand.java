package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class NameCommand extends Command {

    public NameCommand() {
        super("name", "Changes the name of client. Usage: .name <name>.", "n", "nam", "mne", "nome", "brand", "changename", "тфьу");
    }

    @Override
    public void execute(String[] args) {
        String prefix = COMMAND_MANAGER.getExecutor().getPrefix();

        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(NameCommand.class.getName(),
                    CAT_FORMAT.format("Usage: {global}" + prefix + "name <name>{reset}."));
            return;
        }

        String newName = args[0].trim();

        if (newName.isEmpty()) {
            CHAT_MANAGER.sendPersistent(NameCommand.class.getName(),
                    CAT_FORMAT.format("Name cannot be empty."));
            return;
        }

        if (newName.length() > 24) {
            CHAT_MANAGER.sendPersistent(NameCommand.class.getName(),
                    CAT_FORMAT.format("Name is too long."));
            return;
        }

        DISPLAY_NAME = newName;
        CONFIG_MANAGER.saveName(newName);

        CHAT_MANAGER.sendPersistent(NameCommand.class.getName(),
                CAT_FORMAT.format("Name set to: {global}" + newName + "{reset}."));
    }
}
