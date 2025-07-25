package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import net.minecraft.text.MutableText;

import java.util.List;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "Displays list of available commands. Usage: .help <Command>.",
                "h", "?", "hlp", "halp", "hilp", "heil", "рудз", "commands", "command");
    }

    @Override
    public void execute(String[] args) {
        String prefix = COMMAND_MANAGER.getExecutor().getPrefix();

        if (args.length == 0) {
            List<Command> cmds = COMMAND_MANAGER.getStorage().getCommands();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cmds.size(); i++) {
                sb.append("{global}").append(cmds.get(i).getName()).append("{reset}");
                if (i < cmds.size() - 1) sb.append(", ");
            }
            sb.append(".");

            MutableText message = CAT_FORMAT.format("Available commands: " + sb.toString());
            CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(), message);

        } else if (args.length == 1) {
            String search = args[0].toLowerCase();

            for (Command cmd : COMMAND_MANAGER.getStorage().getCommands()) {
                if (cmd.getName().equalsIgnoreCase(search) || cmd.matches(search)) {
                    String usageMsg = cmd.getName() + " usage: {global}" + cmd.getDescription() + "{reset}.";
                    MutableText msg = CAT_FORMAT.format(usageMsg);
                    CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(), msg);
                    return;
                }
            }
            CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(),
                    CAT_FORMAT.format("Command not found: {global}" + search + "{reset}."));

        } else {
            CHAT_MANAGER.sendPersistent(HelpCommand.class.getName(),
                    CAT_FORMAT.format("Type {global}" + prefix + "help{reset}."));
        }
    }
}