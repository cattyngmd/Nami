package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import net.minecraft.text.MutableText;

import java.util.List;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class HelpCommand extends Command {

    public HelpCommand() {
        super("help",
                new CommandArgument[] {},
                "h", "?", "hlp", "halp", "hilp", "heil", "commands", "command");
    }

    @Override
    public void execute(Object[] args) {
        List<Command> cmds = COMMAND_MANAGER.getStorage().getCommands();

        if (cmds.isEmpty()) {
            CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(),
                    CAT_FORMAT.format("No commands registered."));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cmds.size(); i++) {
            String display = cmds.get(i).getName() == null ? "" : cmds.get(i).getName().replace(" ", "");
            sb.append("{g}").append(display).append("{reset}");
            if (i < cmds.size() - 1) sb.append(", ");
        }
        sb.append(".");

        MutableText message = CAT_FORMAT.format("Available commands: " + sb.toString());
        CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(), message);
    }
}
