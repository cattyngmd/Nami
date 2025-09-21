package me.kiriyaga.nami.core.command;

import net.minecraft.command.CommandSource;

import static me.kiriyaga.nami.Nami.*;

public class CommandManager {

    private final CommandStorage storage = new CommandStorage();
    private final CommandExecutor executor = new CommandExecutor(storage);
    private final CommandSuggester suggester = new CommandSuggester(storage);

    public void init() {
        CommandRegistry.registerAnnotatedCommands(storage);
        suggester.registerCommands();
        EVENT_MANAGER.register(executor);
        LOGGER.info("Registered " + storage.size() + " commands.");
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

    // No longer need a custom command source, we use the one from Minecraft
}
