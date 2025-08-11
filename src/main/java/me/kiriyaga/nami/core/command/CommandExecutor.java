package me.kiriyaga.nami.core.command;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.ChatMessageEvent;
import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;

import static me.kiriyaga.nami.Nami.*;

public class CommandExecutor {

    private final CommandStorage storage;
    private String prefix = "-";

    public CommandExecutor(CommandStorage storage) {
        this.storage = storage;
    }

    public void setPrefix(String prefix) {
        if (prefix != null && !prefix.isEmpty()) {
            this.prefix = prefix;
            LOGGER.info("Command prefix changed to: " + prefix);
        } else {
            LOGGER.warn("Attempted to set empty or null prefix.");
        }
    }

    public String getPrefix() {
        return prefix;
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent event) {
        String message = event.getMessage();

        if (!message.startsWith(prefix)) return;

        event.setCancelled(true);

        String[] parts = message.split("\\s+");
        if (parts.length == 0) return;

        String cmdName = parts[0].substring(prefix.length());
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        Command command = storage.getCommandByNameOrAlias(cmdName);
        if (command == null) {
            CHAT_MANAGER.sendPersistent(CommandExecutor.class.getName(),
                    CAT_FORMAT.format("Unknown command: {g}" + cmdName + "{reset}. Use {g}" + prefix + "help{reset}."));
            return;
        }

        CommandArgument[] expected = command.getArguments();
        Object[] parsed = new Object[expected.length];

        try {
            int requiredCount = 0;
            for (CommandArgument arg : expected) {
                if (arg.isRequired()) requiredCount++;
            }
            if (args.length < requiredCount) {
                throw new IllegalArgumentException("Missing required arguments.");
            }

            for (int i = 0; i < expected.length; i++) {
                CommandArgument arg = expected[i];

                if (i >= args.length) {
                    if (!arg.isRequired()) {
                        parsed[i] = null;
                        continue;
                    } else {
                        throw new IllegalArgumentException("Missing argument: " + arg.getName());
                    }
                }

                if (i == expected.length - 1 && arg instanceof CommandArgument.StringArg stringArg) {
                    parsed[i] = stringArg.parse(args, i);
                    break;
                }

                String input = args[i];
                if (arg instanceof CommandArgument.ActionArg actionArg) {
                    parsed[i] = actionArg.getCanonical(input);
                } else {
                    parsed[i] = arg.parse(input);
                }
            }

            command.execute(parsed);

        } catch (IllegalArgumentException e) {
            String argsFormatted = "";
            for (CommandArgument arg : expected) {
                argsFormatted += "<{g}" + arg.getName() + "{s}> ";
            }
            argsFormatted = argsFormatted.trim();

            String usage = "Wrong input! Usage: {s}" + argsFormatted + "{reset}.";

            CHAT_MANAGER.sendPersistent(CommandExecutor.class.getName(),
                    CAT_FORMAT.format(usage));

        } catch (Exception e) {
            LOGGER.error("Error executing command " + command.getName(), e);
        }
    }
}
