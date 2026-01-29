package namidevelopment.kiriyaga.nami.api.command;

import namidevelopment.kiriyaga.nami.impl.command.Command;

import static namidevelopment.kiriyaga.nami.Nami.*;

public class CommandService {

    private final CommandStorage storage = new CommandStorage();
    private final CommandExecutor executor = new CommandExecutor(storage);
    private final CommandSuggester suggester = new CommandSuggester(storage);

    public void init() {
        CommandRegistry.registerAnnotatedCommands(storage);
        CommandRegistry.registerFeatureCommands(storage);
        suggester.updateDispatcher();
        EVENT_SERVICE.register(executor);
        LOGGER.info("Registered " + storage.size() + " commands.");
        LOGGER.info("Command SERVICE loaded.");
    }

    public CommandStorage getStorage() {
        return storage;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public CommandSuggester getSuggester() {
        return suggester;
    }

    public void addCommand(Command command) {
        storage.addCommand(command);
        suggester.updateDispatcher();
    }

    public void removeCommand(Command command) {
        storage.removeCommand(command);
        suggester.updateDispatcher();
    }

}
