package namidevelopment.kiriyaga.api.api.command;


import static namidevelopment.kiriyaga.api.NamiApi.*;

public class CommandService {

    private final CommandStorage storage = new CommandStorage();
    private final CommandExecutor executor = new CommandExecutor(storage);
    private final CommandSuggester suggester = new CommandSuggester(storage);

    public void init() {
        CommandRegistry.registerAnnotatedCommands(storage);
        CommandRegistry.registerFeatureCommands(storage);
        suggester.updateDispatcher();
        EVENT_SERVICE.register(executor);
        API_LOGGER.info("Registered " + storage.size() + " commands.");
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
