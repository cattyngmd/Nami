package namidevelopment.kiriyaga.api.core.command;

import com.mojang.brigadier.CommandDispatcher;
import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandSource;

public class CommandSuggester {

    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
    private final CommandStorage storage;

    public CommandSuggester(CommandStorage storage) {
        this.storage = storage;
    }

    public void rebuild() {
        dispatcher.getRoot().getChildren().clear();

        for (Command cmd : storage.getCommands()) {
            cmd.register(dispatcher);
        }
    }

    public CommandDispatcher<CommandSource> getDispatcher() {
        return dispatcher;
    }
}
