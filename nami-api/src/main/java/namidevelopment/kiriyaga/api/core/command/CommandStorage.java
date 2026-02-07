package namidevelopment.kiriyaga.api.core.command;

import namidevelopment.kiriyaga.api.model.command.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandStorage {

    private final List<Command> commands = new ArrayList<>();

    public void addCommand(Command command) {
        commands.add(command);
    }

    public void removeCommand(Command command) {
        commands.remove(command);
    }

    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    public Command getCommand(String name) {
        String lowerName = name.toLowerCase();
        String lowerNoSpaces = lowerName.replace(" ", "");
        for (Command cmd : commands) {
            String cmdName = cmd.getName();
            if (cmdName != null) {
                String cmdNameLower = cmdName.toLowerCase();
                if (cmdNameLower.equals(lowerName) || cmdNameLower.equals(lowerNoSpaces) || cmdNameLower.replace(" ", "").equals(lowerName)) return cmd;
            }
        }
        return null;
    }

    public int size() {
        return commands.size();
    }
}
