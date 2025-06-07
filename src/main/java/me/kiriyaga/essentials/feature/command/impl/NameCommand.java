package me.kiriyaga.essentials.feature.command.impl;

import me.kiriyaga.essentials.feature.command.Command;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.CONFIG_MANAGER;
import static me.kiriyaga.essentials.Essentials.NAME;


public class NameCommand extends Command {

    public NameCommand() {
        super("name","Changes the name of client. Usage: .name <Name>.", "n", "nam", "mne", "nome", "brand", "changename", "тфьу");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(NameCommand.class.getName(), "Usage: .name <Name>.");
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

        NAME = newName;
        CONFIG_MANAGER.save();

        CHAT_MANAGER.sendPersistent(NameCommand.class.getName(), "Name set to: §7" + newName + "§f");
    }
}
