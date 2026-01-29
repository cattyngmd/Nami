package namidevelopment.kiriyaga.nami.api.command;

import namidevelopment.kiriyaga.nami.impl.command.impl.FeatureCommand;
import namidevelopment.kiriyaga.nami.util.ClasspathScanner;
import namidevelopment.kiriyaga.nami.impl.command.Command;
import namidevelopment.kiriyaga.nami.impl.command.RegisterCommand;

import java.util.Set;

import static namidevelopment.kiriyaga.nami.Nami.*;

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

    public static void registerFeatureCommands(CommandStorage storage) {
        FEATURE_SERVICE.getStorage().getAll().forEach(Feature -> {
            try {
                String name = Feature.getName().replace(" ", "");
                if (storage.getCommandByNameOrAlias(name) == null) {
                    storage.addCommand(new FeatureCommand(Feature));
                }
            } catch (Exception e) {
                LOGGER.error("Failed to initiate command for Feature: " + Feature.getName(), e);
            }
        });
    }
}