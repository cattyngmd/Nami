package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import java.util.List;

import static me.kiriyaga.nami.Nami.COMMAND_MANAGER;
import static me.kiriyaga.nami.Nami.CHAT_MANAGER;

@RegisterCommand
public class HelpCommand extends Command {

    public HelpCommand() {
        super("help","Displays list of available commands. Usage: .help <Command>.", "h", "?", "hlp", "halp", "hilp", "heil", "рудз", "commands", "command");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            StringBuilder builder = new StringBuilder("Available commands: ");
            List<Command> cmds = COMMAND_MANAGER.getCommands();
            for (int i = 0; i < cmds.size(); i++) {
                builder.append("§7").append(cmds.get(i).getName()).append("§f");
                if (i < cmds.size() - 1) {
                    builder.append(", ");
                }
            }
            CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(), builder.toString());
        } else if (args.length == 1) {
            String search = args[0].toLowerCase();
            for (Command cmd : COMMAND_MANAGER.getCommands()) {
                if (cmd.getName().equalsIgnoreCase(search) || cmd.matches(search)) {
                    CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(), "§7" + cmd.getName() + "§f usage: " + cmd.getDescription());

                    return;
                }
            }
            CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(), "Command not found: §7" + search + "§f");
        } else {
            CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(), "Type .help");

        }
    }

}
