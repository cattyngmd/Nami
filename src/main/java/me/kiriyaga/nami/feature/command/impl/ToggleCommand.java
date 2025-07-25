package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.module.Module;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle",
                new CommandArgument[] {
                        new CommandArgument.StringArg("moduleName", 1, 25)
                },
                "on", "off", "switch", "togle", "turnon", "turnoff", "tggle");
    }

    @Override
    public void execute(Object[] args) {
        if (args.length < 1) {
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Usage: {s}" + COMMAND_MANAGER.getExecutor().getPrefix() + "{g}toggle {s}<{g}moduleName{s}>{reset}."));
            return;
        }

        String input = args[0].toString();

        Module found = null;
        for (Module m : MODULE_MANAGER.getStorage().getAll()) {
            if (m.matches(input)) {
                found = m;
                break;
            }
        }

        if (found == null) {
            CHAT_MANAGER.sendTransient(
                    CAT_FORMAT.format("Module '{g}" + input + "{reset}' not found."));
            return;
        }

        found.toggle();
    }
}