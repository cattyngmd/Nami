package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class FriendCommand extends Command {

    public FriendCommand() {
        super(
                "friend",
                new CommandArgument[] {
                        new CommandArgument.ActionArg("action", "add", "del", "remove"),
                        new CommandArgument.StringArg("name", 1, 32)
                },
                "f", "friends", "акшутв"
        );
    }

    @Override
    public void execute(Object[] args) {
        String action = (String) args[0];
        String name = (String) args[1];

        switch (action) {
            case "add" -> FRIEND_MANAGER.addFriend(name);
            case "del" -> FRIEND_MANAGER.removeFriend(name);
        }

        CHAT_MANAGER.sendPersistent(
                FriendCommand.class.getName(),
                CAT_FORMAT.format("Friend list updated: {g}" + action + " {s}" + name + "{reset}.")
        );
    }
}