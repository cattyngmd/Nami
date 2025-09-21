package me.kiriyaga.nami.core.command;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.impl.*;

import java.util.Arrays;
import java.util.List;

public class CommandRegistry {

    // Restoring the full, manual list of all commands.
    private static final List<Class<? extends Command>> COMMAND_CLASSES = Arrays.asList(
        BindCommand.class,
        ChangePrefixCommand.class,
        DisconnectCommand.class,
        DrawnCommand.class,
        FovCommand.class,
        FriendCommand.class,
        GammaCommand.class,
        HelpCommand.class,
        LoadCommand.class,
        LoadConfigCommand.class,
        MacroCommand.class,
        ModuleSettingCommand.class,
        NameCommand.class,
        OpenFolderCommand.class,
        PeekCommand.class,
        PitchCommand.class,
        PrinterCommand.class,
        SaveCommand.class,
        SaveConfigCommand.class,
        ToggleCommand.class,
        WhitelistCommand.class,
        YawCommand.class
    );

    public static void registerAnnotatedCommands(CommandStorage storage) {
        for (Class<? extends Command> clazz : COMMAND_CLASSES) {
            try {
                Command command = clazz.getDeclaredConstructor().newInstance();
                storage.addCommand(command);
            } catch (Exception e) {
                System.err.println("Failed to instantiate command: " + clazz.getName());
                e.printStackTrace();
            }
        }
    }
}
