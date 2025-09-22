package me.kiriyaga.nami.core.command;

import me.kiriyaga.nami.util.ClasspathScanner;
import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import java.util.Set;

import static me.kiriyaga.nami.Nami.*;

public class CommandRegistry {

    public static void registerAnnotatedCommands(CommandStorage storage) {
        Set<Class<? extends Command>> classes = ClasspathScanner.findAnnotated(Command.class, RegisterCommand.class);

        for (Class<? extends Command> clazz : classes) {
            try {
                Command command = clazz.getDeclaredConstructor().newInstance();
                storage.addCommand(command);
            } catch (Exception e) {
                LOGGER.error("Failed to instantiate command: " + clazz.getName(), e);
            }
        }
    }
}