package namidevelopment.kiriyaga.api.core.command;

import namidevelopment.kiriyaga.api.annotation.RegisterCommand;
import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.util.ClasspathScanner;

import java.util.Set;
import static namidevelopment.kiriyaga.api.NamiApi.*;

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