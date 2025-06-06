package me.kiriyaga.essentials.feature.command.commands;

import me.kiriyaga.essentials.feature.command.Command;

import java.util.List;

import static me.kiriyaga.essentials.Essentials.COMMAND_MANAGER;
import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;

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
                builder.append("§8").append(cmds.get(i).getName()).append("§f");
                if (i < cmds.size() - 1) {
                    builder.append(", ");
                }
            }
            CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(), builder.toString());
        } else if (args.length == 1) {
            String search = args[0].toLowerCase();
            for (Command cmd : COMMAND_MANAGER.getCommands()) {
                if (cmd.getName().equalsIgnoreCase(search) || cmd.matches(search)) {
                    CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(), "§8" + cmd.getName() + "§f usage: " + cmd.getDescription());

                    return;
                }
            }
            CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(), "Command not found: §8" + search + "§f");
        } else {
            CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(), "Type .help");

        }
    }

}
