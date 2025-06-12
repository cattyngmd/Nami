package me.kiriyaga.essentials.feature.command.impl;

import me.kiriyaga.essentials.feature.command.Command;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.FRIEND_MANAGER;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", "Manage your friends. Usage: .friend <add|del> <name>", "f", "friends", "акшутв");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 2) {
            CHAT_MANAGER.sendPersistent(FriendCommand.class.getName(), "Usage: .friend <add|del> <name>");
            return;
        }

        String action = args[0].toLowerCase();
        String name = args[1];

        switch (action) {
            case "add" -> {
                FRIEND_MANAGER.addFriend(name);
            }
            case "del", "remove" -> {
                FRIEND_MANAGER.removeFriend(name);
            }
            default -> CHAT_MANAGER.sendPersistent(FriendCommand.class.getName(), "Unknown action: §7" + action);
        }
    }
}
