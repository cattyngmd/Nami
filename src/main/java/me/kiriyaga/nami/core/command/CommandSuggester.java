package me.kiriyaga.nami.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import net.minecraft.command.CommandSource;

public class CommandSuggester {

    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
    private final CommandStorage storage;

    public CommandSuggester(CommandStorage storage) {
        this.storage = storage;
    }

    public void registerCommands() {
        for (Command command : storage.getCommands()) {
            LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(command.getName());
            CommandArgument[] args = command.getArguments();

            if (args.length == 0) {
                builder.executes(context -> {
                    command.execute(new Object[0]);
                    return 1;
                });
            } else {
                com.mojang.brigadier.builder.ArgumentBuilder<CommandSource, ?> argumentChain = null;
                for (int i = args.length - 1; i >= 0; i--) {
                    CommandArgument arg = args[i];
                    boolean isLast = (i == args.length - 1);

                    RequiredArgumentBuilder<CommandSource, ?> argBuilder = RequiredArgumentBuilder.argument(arg.getName(), toBrigadierArgument(arg, isLast));

                    if (arg instanceof CommandArgument.ActionArg actionArg) {
                        argBuilder.suggests((context, suggestionBuilder) -> {
                            for (String value : actionArg.getAllowedValues()) {
                                if (value.toLowerCase().startsWith(suggestionBuilder.getRemaining().toLowerCase())) {
                                    suggestionBuilder.suggest(value);
                                }
                            }
                            return suggestionBuilder.buildFuture();
                        });
                    }

                    if (argumentChain == null) {
                        argBuilder.executes(context -> {
                            Object[] parsedArgs = new Object[args.length];
                            for (int j = 0; j < args.length; j++) {
                                CommandArgument currentArg = args[j];
                                Object brigadierValue = context.getArgument(currentArg.getName(), Object.class);
                                String stringValue = String.valueOf(brigadierValue);
                                parsedArgs[j] = currentArg.parse(stringValue);
                            }
                            command.execute(parsedArgs);
                            return 1;
                        });
                    } else {
                        argBuilder.then(argumentChain);
                    }
                    argumentChain = argBuilder;
                }
                builder.then(argumentChain);
            }

            // Register only the main command name, no aliases.
            dispatcher.register(builder);
        }
    }

    private ArgumentType<?> toBrigadierArgument(CommandArgument arg, boolean isLast) {
        if (arg instanceof CommandArgument.StringArg) {
            return isLast ? StringArgumentType.greedyString() : StringArgumentType.string();
        } else if (arg instanceof CommandArgument.ActionArg) {
            return StringArgumentType.string();
        } else if (arg instanceof CommandArgument.IntArg) {
            return IntegerArgumentType.integer();
        } else if (arg instanceof CommandArgument.DoubleArg) {
            return DoubleArgumentType.doubleArg();
        }
        return StringArgumentType.string();
    }

    public CommandDispatcher<CommandSource> getDispatcher() {
        return dispatcher;
    }
}
