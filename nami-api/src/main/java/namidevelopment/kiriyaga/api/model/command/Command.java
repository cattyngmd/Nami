package namidevelopment.kiriyaga.api.model.command;

import com.mojang.brigadier.CommandDispatcher;
import namidevelopment.kiriyaga.api.core.command.BrigadierCommandAdapter;

public abstract class Command {
    protected final String name;
    protected final CommandArgument[] args;

    public Command(String name, CommandArgument[] args) {
        this.name = name;
        this.args = args;
    }

    public String getName() { return name; }
    public CommandArgument[] getArguments() { return args; }

    public boolean matches(String input) {
        String lower = input.toLowerCase();
        if (lower.equals(name)) return true;
        return false;
    }

    public void register(CommandDispatcher<CommandSource> dispatcher) {
        BrigadierCommandAdapter.register(dispatcher, this);
    }

    public abstract void execute(Object[] parsedArgs);
}
