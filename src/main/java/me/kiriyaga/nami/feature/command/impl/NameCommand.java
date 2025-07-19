package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class NameCommand extends Command {

    public NameCommand() {
        super("name","Changes the name of client. Usage: .name <Name>.", "n", "nam", "mne", "nome", "brand", "changename", "тфьу");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(NameCommand.class.getName(), "Usage: .name §7<Name>");
            return;
        }

        String newName = args[0].trim();

        if (newName.isEmpty()) {
            CHAT_MANAGER.sendPersistent(NameCommand.class.getName(), "Name cannot be empty.");
            return;
        }

        if (newName.length() > 16) {
            CHAT_MANAGER.sendPersistent(NameCommand.class.getName(), "Name is too long.");
            return;
        }

        DISPLAY_NAME = newName;
        CONFIG_MANAGER.save();

        CHAT_MANAGER.sendPersistent(NameCommand.class.getName(), "Name set to: §7" + newName + "§f");
    }
}
