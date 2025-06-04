package me.kiriyaga.essentials.manager;

import me.kiriyaga.essentials.feature.command.Command;
import me.kiriyaga.essentials.Essentials;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.events.ChatMessageEvent;
import me.kiriyaga.essentials.feature.command.commands.HelpCommand;

import java.util.ArrayList;
import java.util.List;

import static me.kiriyaga.essentials.Essentials.LOGGER;
import static me.kiriyaga.essentials.Essentials.EVENT_MANAGER;

public class CommandManager {

    private final List<Command> commands = new ArrayList<>();
    private String prefix = ".";

    public void init() {
        EVENT_MANAGER.register(this);

        registerCommand(new HelpCommand());

        LOGGER.info("Registered " + commands.size() + " commands");
    }

    public void registerCommand(Command command) {
        commands.add(command);
    }

    public void unregisterCommand(Command command) {
        commands.remove(command);
    }

    public List<Command> getCommands() {
        return commands;
    }

    public String getPrefix() {
        return prefix;
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent event) {
        String message = event.getMessage();

        if (!message.startsWith(prefix)) return;

        event.setCancelled(true);

        String withoutPrefix = message.substring(prefix.length()).trim();
        if (withoutPrefix.isEmpty()) return;

        String[] parts = withoutPrefix.split("\\s+");
        String cmdName = parts[0];
        String[] args = new String[0];
        if (parts.length > 1) {
            args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, args.length);
        }

        for (Command cmd : commands) {
            if (cmd.matches(cmdName)) {
                try {
                    cmd.execute(args);
                } catch (Exception e) {
                    LOGGER.error("Error executing command " + cmdName, e);
                    cmd.sendMessage("Error executing command.");
                }
                return;
            }
        }
    }
}
