package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", "Manage your friends. Usage: .friend <add|del> <name>", "f", "friends", "акшутв");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 2) {
            String prefix = COMMAND_MANAGER.getExecutor().getPrefix();
            CHAT_MANAGER.sendPersistent(FovCommand.class.getName(),
                    CAT_FORMAT.format("Usage: {s}" + prefix + "{g}friend {s}<{g}add{s}|{g}del{s}> <{g}name{s}>{reset}."));
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
            default -> CHAT_MANAGER.sendPersistent(FovCommand.class.getName(),
                    CAT_FORMAT.format("Unknown action: "+action+"{reset}."));
        }
    }
}
