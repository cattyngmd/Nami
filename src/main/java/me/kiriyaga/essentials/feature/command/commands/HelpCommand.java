package me.kiriyaga.essentials.feature.command.commands;

import me.kiriyaga.essentials.Essentials;
import me.kiriyaga.essentials.feature.command.Command;
import me.kiriyaga.essentials.manager.CommandManager;

import java.util.List;

import static me.kiriyaga.essentials.Essentials.COMMAND_MANAGER;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help","Displays list of available commands. Usage: .help command?.", "h", "?", "hlp", "halp", "hilp", "heil");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            StringBuilder builder = new StringBuilder("Available commands: ");
            List<Command> cmds = COMMAND_MANAGER.getCommands();
            for (int i = 0; i < cmds.size(); i++) {
                builder.append(cmds.get(i).getName());
                if (i < cmds.size() - 1) {
                    builder.append(", ");
                }
            }
            sendMessage(builder.toString());
        } else if (args.length == 1) {
            String search = args[0].toLowerCase();
            for (Command cmd : COMMAND_MANAGER.getCommands()) {
                if (cmd.getName().equalsIgnoreCase(search) || cmd.matches(search)) {
                    sendMessage(cmd.getName() + " usage: " + cmd.getDescription());
                    return;
                }
            }
            sendMessage("Command not found: " + search);
        } else {
            sendMessage("Type .help");
        }
    }

}
