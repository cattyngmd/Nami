package me.kiriyaga.essentials.feature.command.impl;

import me.kiriyaga.essentials.feature.command.Command;
import me.kiriyaga.essentials.mixininterface.ISimpleOption;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class GammaCommand extends Command {

    public GammaCommand() {
        super("gamma", "Changes your gamma (brightness). Usage: .gamma <Value>", "light", "brightens", "bright", "пфььф");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(GammaCommand.class.getName(), "Usage: .gamma <Value>");
            return;
        }

        try {
            double newGamma = Double.parseDouble(args[0].trim());

            if (newGamma < 0.0 || newGamma > 420.0) {
                CHAT_MANAGER.sendPersistent(GammaCommand.class.getName(), "Gamma must be between 0.0 and 420.0.");
                return;
            }

            ((ISimpleOption) (Object) MINECRAFT.options.getGamma()).setValue(newGamma);

        } catch (NumberFormatException e) {
            CHAT_MANAGER.sendPersistent(GammaCommand.class.getName(), "Invalid number format.");
        }
    }
}
