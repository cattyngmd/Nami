package me.kiriyaga.nami.core.command;

import me.kiriyaga.nami.Nami;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.ChatMessageEvent;
import me.kiriyaga.nami.feature.command.Command;
import net.minecraft.command.CommandSource;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.LOGGER;

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

        String content = message.substring(prefix.length());
        if (content.isEmpty()) return;

        String[] parts = content.split("\\s+", 2);
        String commandName = parts[0];

        Command command = storage.getCommandByNameOrAlias(commandName);

        String commandToExecute;
        if (command != null) {
            // If an alias was used, replace it with the main command name for execution
            String mainCommandName = command.getName();
            if (parts.length > 1) {
                commandToExecute = mainCommandName + " " + parts[1];
            } else {
                commandToExecute = mainCommandName;
            }
        } else {
            commandToExecute = content;
        }

        CommandSource source = Nami.MC.getNetworkHandler().getCommandSource();

        try {
            Nami.COMMAND_MANAGER.getSuggester().getDispatcher().execute(commandToExecute, source);
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(CommandExecutor.class.getName(), e.getMessage());
        }
    }
}
