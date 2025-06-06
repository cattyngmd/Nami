package me.kiriyaga.essentials.feature.command.commands;

import me.kiriyaga.essentials.feature.command.Command;
import me.kiriyaga.essentials.feature.module.Module;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle", "Toggles a module on or off. Usage: .toggle <moduleName>", "on", "off", "switch", "togle", "turnon", "turnoff", "tggle");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            CHAT_MANAGER.sendTransient("Usage: .toggle <moduleName>");
            return;
        }

        String input = args[0];

        Module found = null;
        for (Module m : MODULE_MANAGER.getModules()) {
            if (m.matches(input)) {
                found = m;
                break;
            }
        }

        if (found == null) {
            CHAT_MANAGER.sendTransient("Module '§8" + input + "§f' not found.");
            return;
        }

        found.toggle();
    }
}
